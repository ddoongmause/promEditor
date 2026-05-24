// 템플릿 편집 화면

var templateId = null;

document.addEventListener('DOMContentLoaded', function () {
    // URL에서 templateId 추출 (/templates/{id}/edit 또는 /templates/new)
    var parts = location.pathname.split('/');
    var idIndex = parts.indexOf('templates') + 1;

    if (parts[idIndex] && parts[idIndex] !== 'new') {
        templateId = parseInt(parts[idIndex], 10);
        loadTemplate(templateId);
    }

    document.getElementById('btn-save').addEventListener('click', saveTemplate);

    var btnDelete = document.getElementById('btn-delete');
    var btnClone  = document.getElementById('btn-clone');
    if (templateId) {
        btnDelete.style.display = '';
        btnDelete.addEventListener('click', deleteTemplate);
        btnClone.style.display = '';
        btnClone.addEventListener('click', cloneTemplate);
    }

    // 블록 변경 시 미리보기 갱신
    document.getElementById('block-list').addEventListener('input', updatePreview);
});

// ── 템플릿 불러오기 ────────────────────────────────

function loadTemplate(id) {
    fetch('/api/templates/' + id)
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { alert('템플릿을 불러오지 못했습니다.'); return; }
            var t = body.data;
            document.getElementById('page-title').textContent = '템플릿 편집';
            document.getElementById('title').value = t.title || '';
            document.getElementById('description').value = t.description || '';
            document.getElementById('category').value = t.category || '';
            document.getElementById('tags').value = (t.tags || []).join(', ');

            for (var i = 0; i < t.blocks.length; i++) {
                var b = t.blocks[i];
                if (b.blockType === 'FIXED') {
                    addFixedBlock(b.content);
                } else {
                    addSlotBlock(b.content, b.slotType, b.options);
                }
            }
            updatePreview();
        });
}

// ── 블록 추가 ─────────────────────────────────────

function addFixedBlock(content) {
    var tpl = document.getElementById('tpl-fixed-block');
    var node = tpl.content.cloneNode(true);
    if (content) {
        node.querySelector('.block-content').value = content;
    }
    var blockList = document.getElementById('block-list');
    // 빈 상태 메시지 제거
    var empty = blockList.querySelector('.empty-state');
    if (empty) blockList.removeChild(empty);

    blockList.appendChild(node);
    makeBlockDraggable(blockList.lastElementChild);
    updatePreview();
}

function addSlotBlock(name, slotType, options) {
    var tpl = document.getElementById('tpl-slot-block');
    var node = tpl.content.cloneNode(true);

    if (name)     node.querySelector('.slot-name').value = name;
    if (slotType) node.querySelector('.slot-type').value = slotType;

    var blockList = document.getElementById('block-list');
    var empty = blockList.querySelector('.empty-state');
    if (empty) blockList.removeChild(empty);

    blockList.appendChild(node);

    // SELECT 타입이면 선택지 영역 표시 및 옵션 복원
    var addedBlock = blockList.lastElementChild;
    var selectEl = addedBlock.querySelector('.slot-type');
    if (slotType === 'SELECT') {
        addedBlock.querySelector('.select-options').style.display = '';
        if (options) {
            for (var i = 0; i < options.length; i++) {
                addOption(addedBlock.querySelector('.select-options button'), options[i].label);
            }
        }
    }
    selectEl.addEventListener('change', function () { onSlotTypeChange(this); });
    makeBlockDraggable(addedBlock);
    updatePreview();
}

function removeBlock(btn) {
    var block = btn.closest('.block-item');
    block.parentNode.removeChild(block);

    var blockList = document.getElementById('block-list');
    if (blockList.children.length === 0) {
        blockList.innerHTML = '<div class="empty-state"><p>블록을 추가해보세요.</p></div>';
    }
    updatePreview();
}

// ── 드래그 앤 드롭 ────────────────────────────────

function makeBlockDraggable(block) {
    block.setAttribute('draggable', 'true');
    var handle = block.querySelector('.block-handle');
    handle.style.cursor = 'grab';

    block.addEventListener('dragstart', function (e) {
        e.dataTransfer.effectAllowed = 'move';
        block.classList.add('dragging');
        e.dataTransfer.setData('text/plain', '');
        handle.style.opacity = '0.4';
    });

    block.addEventListener('dragend', function () {
        block.classList.remove('dragging');
        handle.style.opacity = '';
        document.querySelectorAll('.block-over').forEach(function (el) {
            el.classList.remove('block-over');
        });
        updatePreview();
    });

    block.addEventListener('dragover', function (e) {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
        if (!block.classList.contains('dragging')) {
            block.classList.add('block-over');
        }
    });

    block.addEventListener('dragleave', function () {
        block.classList.remove('block-over');
    });

    block.addEventListener('drop', function (e) {
        e.preventDefault();
        block.classList.remove('block-over');
        var dragging = document.querySelector('.dragging');
        if (!dragging || dragging === block) return;

        var blockList = document.getElementById('block-list');
        var allBlocks = Array.from(blockList.querySelectorAll('.block-item:not(.dragging)'));
        var draggedBlock = dragging;

        var insertIndex = -1;
        for (var i = 0; i < allBlocks.length; i++) {
            if (allBlocks[i] === block) {
                insertIndex = i;
                break;
            }
        }
        if (insertIndex >= 0) {
            blockList.insertBefore(draggedBlock, allBlocks[insertIndex]);
        } else {
            blockList.appendChild(draggedBlock);
        }
        updatePreview();
    });
}

function initDragAndDrop() {
    var blockList = document.getElementById('block-list');
    var blocks = blockList.querySelectorAll('.block-item');
    for (var i = 0; i < blocks.length; i++) {
        makeBlockDraggable(blocks[i]);
    }
}

// ── 슬롯 타입 변경 ────────────────────────────────

function onSlotTypeChange(selectEl) {
    var block = selectEl.closest('.block-item');
    var optionsArea = block.querySelector('.select-options');
    if (selectEl.value === 'SELECT') {
        optionsArea.style.display = '';
    } else {
        optionsArea.style.display = 'none';
    }
    updatePreview();
}

// ── 선택지 추가/삭제 ──────────────────────────────

function addOption(btn, label) {
    var tpl = document.getElementById('tpl-option-row');
    var node = tpl.content.cloneNode(true);
    if (label) node.querySelector('.option-label').value = label;

    var optionList = btn.closest('.select-options').querySelector('.option-list');
    optionList.appendChild(node);
    updatePreview();
}

function removeOption(btn) {
    var row = btn.closest('.option-row');
    row.parentNode.removeChild(row);
    updatePreview();
}

// ── 미리보기 갱신 ──────────────────────────────────

function updatePreview() {
    var blocks = document.querySelectorAll('#block-list .block-item');
    var preview = '';

    for (var i = 0; i < blocks.length; i++) {
        var block = blocks[i];
        var type = block.getAttribute('data-type');

        if (type === 'FIXED') {
            preview += block.querySelector('.block-content').value;
        } else {
            var name = block.querySelector('.slot-name').value || '슬롯';
            var slotType = block.querySelector('.slot-type').value;

            if (slotType === 'SELECT') {
                var options = block.querySelectorAll('.option-label');
                var labels = [];
                for (var j = 0; j < options.length; j++) {
                    if (options[j].value) labels.push(options[j].value);
                }
                preview += '[' + name + ': ' + (labels.join(' / ') || '선택지 없음') + ']';
            } else {
                preview += '[' + name + ']';
            }
        }
    }

    document.getElementById('preview').textContent = preview || '블록을 추가하면 여기에 미리보기가 표시됩니다.';
}

// ── 저장 ──────────────────────────────────────────

function saveTemplate() {
    var title = document.getElementById('title').value.trim();
    if (!title) { alert('템플릿 제목을 입력해주세요.'); return; }

    var blocks = [];
    var blockItems = document.querySelectorAll('#block-list .block-item');

    for (var i = 0; i < blockItems.length; i++) {
        var block = blockItems[i];
        var type = block.getAttribute('data-type');

        if (type === 'FIXED') {
            blocks.push({
                blockType: 'FIXED',
                content: block.querySelector('.block-content').value
            });
        } else {
            var slotType = block.querySelector('.slot-type').value;
            var options = [];
            if (slotType === 'SELECT') {
                var optionEls = block.querySelectorAll('.option-label');
                for (var j = 0; j < optionEls.length; j++) {
                    if (optionEls[j].value.trim()) {
                        options.push({ label: optionEls[j].value.trim() });
                    }
                }
            }
            blocks.push({
                blockType: 'SLOT',
                content: block.querySelector('.slot-name').value.trim(),
                slotType: slotType,
                options: options
            });
        }
    }

    var tagsRaw = document.getElementById('tags').value.trim();
    var tags = tagsRaw ? tagsRaw.split(',').map(function (t) { return t.trim(); }).filter(Boolean) : [];

    var payload = {
        title: title,
        description: document.getElementById('description').value.trim(),
        category: document.getElementById('category').value.trim(),
        tags: tags,
        blocks: blocks
    };

    var method = templateId ? 'PUT' : 'POST';
    var url = templateId ? '/api/templates/' + templateId : '/api/templates';

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { alert('저장 실패: ' + body.message); return; }
            if (!templateId) {
                location.href = '/templates/' + body.data.id + '/edit';
            } else {
                alert('저장되었습니다.');
            }
        })
        .catch(function () { alert('서버 오류가 발생했습니다.'); });
}

// ── 삭제 ──────────────────────────────────────────

function deleteTemplate() {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    fetch('/api/templates/' + templateId, { method: 'DELETE' })
        .then(function (res) {
            if (res.status === 204) {
                location.href = '/templates';
            } else {
                alert('삭제에 실패했습니다.');
            }
        })
        .catch(function () { alert('서버 오류가 발생했습니다.'); });
}

// ── 복제 ──────────────────────────────────────────

function cloneTemplate() {
    if (!confirm('이 템플릿을 복제하시겠습니까?')) return;

    fetch('/api/templates/' + templateId + '/clone', { method: 'POST' })
        .then(function (res) { return res.json(); })
        .then(function (body) {
            if (!body.success) { alert('복제에 실패했습니다.'); return; }
            if (confirm('복제 완료! 복제된 템플릿으로 이동할까요?')) {
                location.href = '/templates/' + body.data.id + '/edit';
            }
        })
        .catch(function () { alert('서버 오류가 발생했습니다.'); });
}
