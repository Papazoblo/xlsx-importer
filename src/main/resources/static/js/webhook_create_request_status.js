$(document).ready(function () {

    $('select[name^=product-]').on('change', function (e) {
        var itemId = $(this).attr("name").split('-')[1];

        var products = [];
        var options = $(this).get(0).selectedOptions;
        for (var i = 0; i < options.length; i++) {
            if (options[i] !== undefined) {
                products.push(options[i].value);
            }
        }

        $.ajax({
            url: '/settings/webhook-create-request-statuses/' + itemId + '/products',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "products": products
            }),
            type: 'PUT',
            error: function (e) {

            }
        });
    });

    $('#btn-add-project').on('click', function () {
        var statusId = $('select[id=statusName]').get(0).selectedOptions[0].value;
        var btn = $(this);
        var bank = $('select[name=bankName]').get(0).selectedOptions[0].value;

        /*if (name.length === 0) {
            alert("Название не указано");
            return;
        }*/

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/webhook-create-request-statuses',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "statusId": statusId,
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
                url: '/settings/webhook-create-request-statuses/' + id,
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