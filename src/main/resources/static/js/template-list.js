// 템플릿 목록 화면

var currentTab = 'all';
var searchTimer = null;

document.addEventListener('DOMContentLoaded', function () {
    loadTemplates();
});

// ── 탭 전환 ───────────────────────────────────────

function switchTab(tab) {
    currentTab = tab;
    document.querySelectorAll('.tab-btn').forEach(function (btn) {
        btn.classList.toggle('active', btn.getAttribute('data-tab') === tab);
    });
    loadTemplates();
}

// ── 검색 (디바운스 300ms) ─────────────────────────

function onSearchInput() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(loadTemplates, 300);
}

// ── 목록 불러오기 ──────────────────────────────────

function loadTemplates() {
    var container = document.getElementById('template-list');
    container.innerHTML = '<div class="empty-state"><p>불러오는 중...</p></div>';

    if (currentTab === 'favorites') {
        fetchFavorites();
        return;
    }

    var keyword  = document.getElementById('search-input').value.trim();
    var category = document.getElementById('category-select').value;

    var url = '/api/templates';
    var params = [];
    if (keyword)  params.push('keyword='  + encodeURIComponent(keyword));
    if (category) params.push('category=' + encodeURIComponent(category));
    if (params.length) url += '?' + params.join('&');

    fetch(url)
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { showError('목록을 불러오지 못했습니다.'); return; }
            buildCategoryOptions(body.data);
            renderList(body.data);
        })
        .catch(function () { showError('서버에 연결할 수 없습니다.'); });
}

function fetchFavorites() {
    fetch('/api/templates/favorites')
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { showError('즐겨찾기를 불러오지 못했습니다.'); return; }
            renderList(body.data);
        })
        .catch(function () { showError('서버에 연결할 수 없습니다.'); });
}

// ── 카테고리 옵션 구성 ─────────────────────────────

function buildCategoryOptions(templates) {
    var select = document.getElementById('category-select');
    var current = select.value;
    var categories = new Set();

    for (var i = 0; i < templates.length; i++) {
        if (templates[i].category) categories.add(templates[i].category);
    }

    var html = '<option value="">전체 카테고리</option>';
    categories.forEach(function (cat) {
        var selected = cat === current ? ' selected' : '';
        html += '<option value="' + escapeHtml(cat) + '"' + selected + '>' + escapeHtml(cat) + '</option>';
    });
    select.innerHTML = html;
}

// ── 목록 렌더링 ────────────────────────────────────

function renderList(templates) {
    var container = document.getElementById('template-list');

    if (templates.length === 0) {
        container.innerHTML = '<div class="empty-state"><p>아직 템플릿이 없습니다.<br>새 템플릿을 만들어보세요!</p></div>';
        return;
    }

    var html = '';
    for (var i = 0; i < templates.length; i++) {
        var t = templates[i];
        html += '<div class="card">';

        // 헤더 (제목 + 즐겨찾기)
        html += '  <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;">';
        html += '    <div class="card-title">' + escapeHtml(t.title) + '</div>';
        html += '    <button class="fav-btn" data-id="' + t.id + '" onclick="toggleFavorite(this,' + t.id + ')"'
                    + ' title="즐겨찾기 토글">'
                    + (t.isFavorite ? '★' : '☆') + '</button>';
        html += '  </div>';

        if (t.description) {
            html += '  <div class="card-description">' + escapeHtml(t.description) + '</div>';
        }

        // 카테고리 + 태그
        if (t.category || (t.tags && t.tags.length > 0)) {
            html += '  <div class="tag-row mt-4">';
            if (t.category) {
                html += '<span class="badge badge-category">' + escapeHtml(t.category) + '</span>';
            }
            if (t.tags) {
                for (var j = 0; j < t.tags.length; j++) {
                    html += '<span class="badge badge-tag">#' + escapeHtml(t.tags[j]) + '</span>';
                }
            }
            html += '  </div>';
        }

        html += '  <div class="mt-4" style="display:flex;gap:8px;">';
        html += '    <a href="/templates/' + t.id + '/use"  class="btn btn-primary"   style="font-size:13px;">사용</a>';
        html += '    <a href="/templates/' + t.id + '/edit" class="btn btn-secondary" style="font-size:13px;">편집</a>';
        html += '    <button class="btn btn-secondary" onclick="exportTemplate(' + t.id + ')" style="font-size:13px;">내보내기</button>';
        html += '  </div>';
        html += '</div>';
    }
    container.innerHTML = html;
}

// ── 즐겨찾기 토글 ──────────────────────────────────

function toggleFavorite(btn, id) {
    fetch('/api/templates/' + id + '/favorite', { method: 'POST' })
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) return;
            btn.textContent = body.data.isFavorite ? '★' : '☆';
            if (currentTab === 'favorites') loadTemplates();
        });
}

// ── 내보내기 (JSON 다운로드) ──────────────────────

function exportTemplate(id) {
    fetch('/api/templates/' + id + '/export')
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { alert('내보내기 실패'); return; }
            var json = JSON.stringify(body.data, null, 2);
            var blob = new Blob([json], { type: 'application/json' });
            var a = document.createElement('a');
            a.href = URL.createObjectURL(blob);
            a.download = 'template-' + id + '.json';
            a.click();
        });
}

// ── 가져오기 (JSON 파일 읽기) ─────────────────────

function importTemplate(input) {
    var file = input.files[0];
    if (!file) return;

    var reader = new FileReader();
    reader.onload = function (e) {
        var data;
        try { data = JSON.parse(e.target.result); }
        catch (err) { alert('올바른 JSON 파일이 아닙니다.'); return; }

        fetch('/api/templates/import', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (!body.success) { alert('가져오기 실패: ' + body.message); return; }
                alert('가져오기 완료: ' + body.data.title);
                loadTemplates();
            });
    };
    reader.readAsText(file);
    input.value = '';
}

// ── 유틸 ──────────────────────────────────────────

function showError(message) {
    document.getElementById('template-list').innerHTML =
        '<div class="empty-state"><p style="color:#ef4444;">' + escapeHtml(message) + '</p></div>';
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
