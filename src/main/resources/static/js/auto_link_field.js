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

    $('input[type=checkbox]').on('click', function () {
        $("#btnSave").removeAttr('disabled');
    });

    function addToTable(table, value) {

        if (value.length === 0) {
            return;
        }
        $("#btnSave").removeAttr('disabled');

        $(table)[0].firstElementChild.innerHTML +=
            "<tr><td class=\"fieldNameValue\">" + value + "</td>" +
            "    <td class=\"deleteFieldNameValue\">" +
            "         <button class=\"btn btn-delete\" title=\"Удалить\">" +
            "              <i class=\"fa-solid fa-circle-minus\"></i>" +
            "         </button>" +
            "    </td></tr>";
    }

    $(".settingNameTable").on('click', '.btn-delete', function () {
        $("#btnSave").removeAttr('disabled');
        $($($(this)[0]).parent()[0]).parent()[0].remove();
    });

    $("#btnSave").on('click', function (e) {
        var elements = $('.fieldName');
        var requestBody = Array();
        for (i = 0; i < elements.length; i++) {
            var fieldName = $(elements[i]).attr('name');
            var tdNameList = $($('.' + fieldName)[0]).next('.settingNameTable').children().children();
            var nameArray = Array();
            for (j = 0; j < tdNameList.length; j++) {
                nameArray.push(tdNameList[j].innerText);
            }

            var checkBoxes = $('input[type=checkbox][name=' + fieldName + ']');

            requestBody.push({
                'field': fieldName,
                'columns': nameArray,
                'required': $(checkBoxes[0]).is(":checked")
            });
        }

        $("#btnSave").attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/auto-link',
            data: JSON.stringify(requestBody),
            dataType: 'json',
            cache: false,
            contentType: "application/json; charset=utf-8",
            processData: false,
            type: 'POST',
            error: function (e) {
                if (e.status === 200) {
                    $("#btnSave").attr('disabled', 'disabled');
                    alert("Настройки сохранены");
                } else {
                    $("#btnSave").removeAttr('disabled');
                    alert("Не удается сохранить настройки");
                }
            }
        });
    });
});