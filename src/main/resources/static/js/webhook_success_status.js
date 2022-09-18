$(document).ready(function () {

    $('#btn-add-project').on('click', function () {
        var name = $('#statusName').val();
        var btn = $(this);
        var bank = $('select[name=bankName]').get(0).selectedOptions[0].value;

        if (name.length === 0) {
            alert("Название не указано");
            return;
        }

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/webhook-success-statuses',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "name": name,
                "bank": bank
            }),
            type: 'POST',
            error: function (e) {
                if (e.status === 200) {
                    window.location.reload();
                } else {
                    alert(e.responseText);
                    $('#btn-add-project').removeAttr('disabled');
                }
            }
        });
    });

    $(".btn-delete").on('click', function () {
        var id = $(this).attr("id");
        var isDelete = confirm("Удалить статус с id = " + id + "?");
        var thCur = $($($($(this).parent())[0]).parent());


        if (isDelete) {
            $.ajax({
                url: '/settings/webhook-success-statuses/' + id,
                dataType: 'json',
                contentType: "application/json; charset=utf-8",
                cache: false,
                type: 'DELETE',
                error: function (e) {
                    if (e.status === 200) {
                        thCur.remove();
                    } else {
                        alert("Невозможно удалить запись");
                    }
                }
            });
        }
    });

    $(".onlyNumbers").keyup(function (e) {
        if (/\D/g.test(this.value)) {
            this.value = this.value.replace(/\D/g, '');
        }
    });
});