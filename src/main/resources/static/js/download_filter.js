$(document).ready(function () {

    $('.settingInput').keypress(function (e) {
        if (e.code === 'Enter' || e.code === 'NumpadEnter') {
            addToTable();
        }
    });

    $('.btnAdd').on('click', function () {
        addToTable();
    });

    function addToTable() {
        var filterValueElement = $('#filterValue');
        var value = filterValueElement.val();
        var bank;
        var bankCode;
        var options = $('#bankName')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank = options[j].text;
                bankCode = options[j].value;
            }
        }
        filterValueElement.val('');

        if (value.length === 0) {
            return;
        }
        $("#btnSave").removeAttr('disabled');

        $('.settingNameTable tbody')[0].innerHTML +=
            "<tr class='valueRow'><td class=\"filterValue\">" + value + "</td>" +
            "    <td class=\"bankName\" name='" + bankCode + "'>" + bank + "</td>" +
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

    $('select[name=filterList]').on('change', function (e) {
        var valueSelected = this.value;
        window.location.replace("/settings/filters?filter=" + valueSelected);
    });

    $("#btnSave").on('click', function (e) {
        var name = $('select[name=filterList]').find(":selected").val();
        var filterArray = Array();
        var values = $(".valueRow")
        console.log(values);
        for (i = 0; i < values.length; i++) {
            console.log(values[i]);
            var valuesTd = $(values[i]).children('td');
            console.log(valuesTd);
            filterArray.push({
                bank: valuesTd[1].getAttribute('name').length === 0 ? null : valuesTd[1].getAttribute('name'),
                value: valuesTd[0].innerText
            });
        }
        var requestBody = {
            'filter': filterArray
        };

        console.log(JSON.stringify(requestBody));

        $("#btnSave").attr('disabled', 'disabled');

        $.ajax({
            url: '/settings/filters/' + name,
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