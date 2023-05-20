$(document).ready(function () {

    $('#startPartsLoad').on('click', function () {

        var dateFrom = formatDate($('#createDateFrom').val());
        var dateTo = formatDate($('#createDateTo').val());
        var bank;
        var count = $('#leadCount').val();

        var options = $('#bankFilter')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank = options[j].value;
                break;
            }
        }

        if (dateFrom.length === 0) {
            alert("Необходимо указать Дату создания с");
            return;
        }

        if (dateTo.length === 0) {
            alert("Необходимо указать Дату создания по");
            return;
        }

        if (count.length === 0) {
            alert("Необходимо указать количество лидов");
            return;
        }


        var btn = $(this);
        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/parts-load',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "dateFrom": dateFrom,
                "dateTo": dateTo,
                "bank": bank,
                "count": count,
                "timeZone": new Date().getTimezoneOffset() / -1
            }),
            type: 'POST',
            error: function (e) {
                btn.removeAttr('disabled');
                if (e.status !== 200) {
                    alert(e.responseText);
                    btn.removeAttr('disabled');
                } else {
                    alert('Загрузка запущена');
                }
            }
        });
    });

    $(".onlyNumbers").keyup(function () {
        if (/\D/g.test(this.value)) {
            this.value = this.value.replace(/\D/g, '');
        }
    });

    function formatDate(dateString) {
        if (dateString !== undefined && dateString.length > 0) {
            var splitAll = dateString.split(" ");
            var splitDate = splitAll[0].split(".");
            return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0] + "T" + splitAll[1];
        }
        return "";
    }
});