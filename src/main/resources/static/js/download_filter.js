$(document).ready(function () {

    $('.settingInput').keypress(function (e) {
        if (e.code === 'Enter' || e.code === 'NumpadEnter') {
            var value = $(this).val();
            $(this).val('');
            addToTable($($($(this)[0].nextElementSibling)[0].nextElementSibling)[0], value);
        }
    });

    $('.btnAdd').on('click', function () {
        var value = $($(this)[0].previousElementSibling).val();
        $($(this)[0].previousElementSibling).val('');
        addToTable($($(this)[0].nextElementSibling)[0], value);
    });

    function addToTable(table, value) {

        if (value.length === 0) {
            return;
        }
        $("#btnSave").removeAttr('disabled');

        $(table)[0].firstElementChild.innerHTML +=
            "<tr><td class=\"filterValue\">" + value + "</td>" +
            "    <td class=\"deleteFilterValue\">" +
            "         <button class=\"btn btn-delete\" title=\"Удалить\">" +
            "              <i class=\"fa-solid fa-circle-minus\"></i>" +
            "         </button>" +
            "    </td></tr>";
    }

    $(".settingNameTable").on('click', '.btn-delete', function () {
        $("#btnSave").removeAttr('disabled');
        $($($(this)[0]).parent()[0]).parent()[0].remove()
    });

    $(".onlyNumbers").keyup(function () {
        if (/\D/g.test(this.value)) {
            this.value = this.value.replace(/\D/g, '');
        }
    });

    $("#btnSave").on('click', function (e) {
        var name = $('select[name=filterList]').find(":selected").val();
        var filterArray = Array();
        var values = $(".filterValue")
        for (i = 0; i < values.length; i++) {
            filterArray.push('\"' + values[i].innerText + "\"");
        }
        var requestBody = {
            'name': name,
            'filter': filterArray
        };

        console.log(JSON.stringify(requestBody));

        $("#btnSave").attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/filters',
            data: JSON.stringify(requestBody),
            dataType: 'json',
            cache: false,
            contentType: "application/json; charset=utf-8",
            processData: false,
            type: 'POST',
            success: function (e) {
                $("#btnSave").attr('disabled', 'disabled');
                alert("Фильтры сохранены");
            },
            error: function (e) {
                if (e.status === 200) {
                    $("#btnSave").attr('disabled', 'disabled');
                    alert("Фильтры сохранены");
                } else {
                    $("#btnSave").removeAttr('disabled');
                    alert("Не удается сохранить фильтры");
                }
            }
        });
    });
});