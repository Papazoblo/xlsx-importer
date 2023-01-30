$(document).ready(function () {

    $('#btn-add-project').on('click', function () {
        var projectId = $('#textProjectId').val();
        var date = $('#date').val();
        var btn = $(this);
        var bank;

        var options = $('#bank')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank = options[j].value;
            }
        }

        if (bank === undefined || bank.length === 0) {
            alert("Банк не указан");
            return;
        }

        if (projectId.length === 0) {
            alert("Номер проекта не указан");
            return;
        }
        if (date.length === 0) {
            alert("Дата для загрузки не указана");
            return;
        }

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/projects',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "date": date,
                "number": projectId,
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
        var isDelete = confirm("Удалить проект с id = " + id + "?");
        var thCur = $($($($(this).parent())[0]).parent());


        if (isDelete) {
            $.ajax({
                url: '/settings/projects/' + id,
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