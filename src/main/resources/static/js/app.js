/* =====================================================
   实验室预约管理系统 · 页面交互脚本
   ===================================================== */
(function () {
    'use strict';

    // 1. 危险操作二次确认
    document.addEventListener('click', function (event) {
        var trigger = event.target.closest('[data-confirm]');
        if (!trigger) {
            return;
        }
        var message = trigger.getAttribute('data-confirm') || '确定执行该操作吗？';
        if (!window.confirm(message)) {
            event.preventDefault();
            event.stopPropagation();
        }
    });

    // 2. 点击空闲时段，自动填入预约表单时间
    var startInput = document.getElementById('startTime');
    var endInput = document.getElementById('endTime');
    document.querySelectorAll('[data-slot-start]').forEach(function (slot) {
        slot.addEventListener('click', function () {
            if (!startInput || !endInput || slot.disabled) {
                return;
            }
            startInput.value = slot.getAttribute('data-slot-start');
            endInput.value = slot.getAttribute('data-slot-end');
            startInput.focus();
        });
    });

    // 3. 列表全选 / 取消全选
    document.querySelectorAll('.check-all').forEach(function (checkAll) {
        checkAll.addEventListener('change', function () {
            var scope = checkAll.closest('table') || document;
            scope.querySelectorAll('.row-check').forEach(function (item) {
                item.checked = checkAll.checked;
            });
        });
    });

    // 4. 批量取消：收集勾选的预约 ID 后提交
    document.querySelectorAll('[data-batch-submit]').forEach(function (button) {
        button.addEventListener('click', function () {
            var form = button.closest('form');
            if (!form) {
                return;
            }
            form.querySelectorAll('input[name="ids"]').forEach(function (input) {
                input.remove();
            });
            var checked = document.querySelectorAll('.row-check:checked');
            if (checked.length === 0) {
                window.alert('请先勾选需要批量处理的预约记录');
                return;
            }
            if (!window.confirm('确定批量强制取消已勾选的 ' + checked.length + ' 条预约吗？')) {
                return;
            }
            checked.forEach(function (item) {
                var hidden = document.createElement('input');
                hidden.type = 'hidden';
                hidden.name = 'ids';
                hidden.value = item.value;
                form.appendChild(hidden);
            });
            form.submit();
        });
    });

    // 5. 选择实验室后自动刷新时段参考
    var labSelect = document.getElementById('labId');
    if (labSelect && document.querySelector('.slot-grid')) {
        labSelect.addEventListener('change', function () {
            var dateInput = document.getElementById('reservationDate');
            var url = new URL(window.location.href);
            if (labSelect.value) {
                url.searchParams.set('labId', labSelect.value);
            } else {
                url.searchParams.delete('labId');
            }
            if (dateInput && dateInput.value) {
                url.searchParams.set('date', dateInput.value);
            }
            window.location.href = url.toString();
        });
    }
})();
