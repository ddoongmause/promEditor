// 프롬프트 사용 화면

var templateData = null;
var templateId = null;

document.addEventListener('DOMContentLoaded', function () {
    // URL에서 templateId 추출 (/templates/{id}/use)
    var parts = location.pathname.split('/');
    templateId = parts[parts.indexOf('templates') + 1];

    if (!templateId) { return; }

    loadTemplate(templateId);
    loadHistory(templateId);
    document.getElementById('btn-copy').addEventListener('click', copyToClipboard);
});

// ── 템플릿 불러오기 ────────────────────────────────

function loadTemplate(id) {
    fetch('/api/templates/' + id)
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) {
                document.getElementById('template-title').textContent = '불러오기 실패';
                return;
            }
            templateData = body.data;
            document.getElementById('template-title').textContent = templateData.title;
            document.getElementById('template-description').textContent = templateData.description || '';
            document.getElementById('btn-edit').href = '/templates/' + id + '/edit';
            renderSlotForm(templateData.blocks);
            updatePreview();
        });
}

// ── 슬롯 입력 폼 렌더링 ───────────────────────────

function renderSlotForm(blocks) {
    var form = document.getElementById('slot-form');
    var html = '';
    var hasSlot = false;

    for (var i = 0; i < blocks.length; i++) {
        var b = blocks[i];
        if (b.blockType !== 'SLOT') continue;
        hasSlot = true;

        html += '<div class="form-group" data-block-id="' + b.id + '">';
        html += '<label class="form-label">' + escapeHtml(b.content || ('슬롯 ' + (i + 1))) + '</label>';

        if (b.slotType === 'SELECT') {
            html += '<select class="form-control slot-input" onchange="updatePreview()">';
            html += '<option value="">-- 선택 --</option>';
            for (var j = 0; j < b.options.length; j++) {
                html += '<option value="' + escapeHtml(b.options[j].label) + '">' + escapeHtml(b.options[j].label) + '</option>';
            }
            html += '</select>';
        } else if (b.slotType === 'TEXTAREA') {
            html += '<textarea class="form-control slot-input" rows="4" placeholder="' + escapeHtml(b.content) + '을(를) 입력하세요" oninput="updatePreview()"></textarea>';
        } else {
            html += '<input type="text" class="form-control slot-input" placeholder="' + escapeHtml(b.content) + '을(를) 입력하세요" oninput="updatePreview()">';
        }

        html += '</div>';
    }

    if (!hasSlot) {
        html = '<p class="text-muted">이 템플릿에는 변수 슬롯이 없습니다.</p>';
    }

    form.innerHTML = html;
}

// ── 미리보기 갱신 ──────────────────────────────────

function updatePreview() {
    if (!templateData) return;

    var slotInputs = document.querySelectorAll('.slot-input');
    var slotValues = {};
    var slotIndex = 0;

    // 슬롯 블록 순서대로 입력값 수집
    for (var i = 0; i < templateData.blocks.length; i++) {
        var b = templateData.blocks[i];
        if (b.blockType === 'SLOT') {
            var input = slotInputs[slotIndex++];
            slotValues[b.id] = input ? input.value : '';
        }
    }

    // 블록 조합
    var result = '';
    for (var i = 0; i < templateData.blocks.length; i++) {
        var b = templateData.blocks[i];
        if (b.blockType === 'FIXED') {
            result += b.content || '';
        } else {
            var val = slotValues[b.id];
            result += val ? val : '[' + (b.content || '슬롯') + ']';
        }
    }

    document.getElementById('preview').textContent = result || '슬롯을 채우면 여기에 결과가 표시됩니다.';
}

// ── 클립보드 복사 ──────────────────────────────────

function copyToClipboard() {
    var text = document.getElementById('preview').textContent;
    if (!text || text === '슬롯을 채우면 여기에 결과가 표시됩니다.') {
        alert('복사할 내용이 없습니다.');
        return;
    }

    navigator.clipboard.writeText(text).then(function () {
        showToast();
        saveHistory();
    }).catch(function () {
        // fallback
        var el = document.createElement('textarea');
        el.value = text;
        document.body.appendChild(el);
        el.select();
        document.execCommand('copy');
        document.body.removeChild(el);
        showToast();
        saveHistory();
    });
}

// ── 히스토리 저장 ──────────────────────────────────

function saveHistory() {
    if (!templateData || !templateId) return;

    var slotInputs = document.querySelectorAll('.slot-input');
    var slotValues = {};
    var slotIndex = 0;

    for (var i = 0; i < templateData.blocks.length; i++) {
        var b = templateData.blocks[i];
        if (b.blockType === 'SLOT') {
            var input = slotInputs[slotIndex++];
            if (input && input.value) {
                slotValues[b.content || ('슬롯' + i)] = input.value;
            }
        }
    }

    fetch('/api/templates/' + templateId + '/histories', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ slotValues: slotValues })
    }).then(function () {
        loadHistory(templateId);
    });
}

// ── 히스토리 불러오기 ──────────────────────────────

function loadHistory(id) {
    fetch('/api/templates/' + id + '/histories')
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success || !body.data.length) return;
            renderHistory(body.data);
        });
}

function renderHistory(histories) {
    var form = document.getElementById('slot-form');
    var section = form.querySelector('.history-section');
    if (section) form.removeChild(section);

    var div = document.createElement('div');
    div.className = 'history-section';

    var title = document.createElement('p');
    title.className = 'form-label';
    title.textContent = '이전 입력 기록';
    div.appendChild(title);

    for (var i = 0; i < histories.length; i++) {
        var h = histories[i];
        var values;
        try { values = JSON.parse(h.slotValues); } catch (e) { continue; }

        var keys = Object.keys(values);
        if (!keys.length) continue;

        var preview = keys.slice(0, 2).map(function (k) {
            return k + ': ' + values[k];
        }).join(' / ');
        if (keys.length > 2) preview += ' …';

        var item = document.createElement('div');
        item.className = 'history-item';
        item.innerHTML = '<span>' + escapeHtml(preview) + '</span>'
            + '<span class="history-time">' + formatTime(h.createdAt) + '</span>';

        item.addEventListener('click', (function (v) {
            return function () { applyHistory(v); };
        })(values));

        div.appendChild(item);
    }
    form.appendChild(div);
}

function applyHistory(values) {
    if (!templateData) return;
    var slotInputs = document.querySelectorAll('.slot-input');
    var slotIndex = 0;
    for (var i = 0; i < templateData.blocks.length; i++) {
        var b = templateData.blocks[i];
        if (b.blockType !== 'SLOT') continue;
        var key = b.content || ('슬롯' + i);
        var input = slotInputs[slotIndex++];
        if (input && values[key] !== undefined) {
            input.value = values[key];
        }
    }
    updatePreview();
}

function formatTime(isoStr) {
    if (!isoStr) return '';
    var d = new Date(isoStr);
    return (d.getMonth() + 1) + '/' + d.getDate() + ' '
         + d.getHours() + ':' + String(d.getMinutes()).padStart(2, '0');
}

function showToast() {
    var toast = document.getElementById('toast');
    toast.style.display = 'block';
    setTimeout(function () { toast.style.display = 'none'; }, 2000);
}

// ── 유틸 ──────────────────────────────────────────

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;');
}
