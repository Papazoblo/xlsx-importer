$(document).ready(function () {

    $('.btn-search').on('click', function () {
        window.location.href = '/contacts?' + getJsonFilter();
    });

    $('.btn-export').on('click', function () {
        //window.location = '/contacts/export?' + getJsonFilter();
        window.open('/contacts/export?' + getJsonFilter())
    });

    function getJsonFilter() {
        var name = $('#nameFilter').val();
        var surname = $('#surnameFilter').val();
        var middleName = $('#middleNameFilter').val();
        var phone = $('#phoneFilter').val();
        var orgName = $('#orgNameFilter').val();
        var inn = $('#innFilter').val();
        var region = $('#regionFilter').val();
        var city = $('#cityFilter').val();
        var createDateFrom = formatDate($('#createDateFromFilter').val());
        var createDateTo = formatDate($('#createDateToFilter').val());
        var ogrn = $('#ogrnFilter').val();
        var status = [];
        var bank = [];
        var original = [];
        var vtbWebhook = [];
        var openingWebhook = [];
        var vtbActuality = [];
        var openingActuality = [];

        var options = $('#bankFilter')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                bank.push(options[j].value);
            }
        }

        options = $('#originalFilter')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                original.push(options[j].value);
            }
        }

        options = $('#statusFilter')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                status.push(options[j].value);
            }
        }

        options = $('#vtbWebhook')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                vtbWebhook.push(options[j].value);
            }
        }

        options = $('#openingWebhook')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                openingWebhook.push(options[j].value);
            }
        }

        options = $('#vtbActuality')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                vtbActuality.push(options[j].value);
            }
        }

        options = $('#openingActuality')[0].selectedOptions;
        for (let j = 0; j < options.length; j++) {
            if (options[j] !== undefined) {
                openingActuality.push(options[j].value);
            }
        }

        return new URLSearchParams({
            "name": name,
            "surname": surname,
            "middleName": middleName,
            "phone": phone,
            "orgName": orgName,
            "inn": inn,
            "region": region,
            "city": city,
            "createDateFrom": createDateFrom,
            "createDateTo": createDateTo,
            "ogrn": ogrn,
            "status": status,
            "bank": bank,
            "original": original,
            "vtbWebhook": vtbWebhook,
            "openingWebhook": openingWebhook,
            "openingActuality": openingActuality,
            "vtbActuality": vtbActuality
        }).toString()
    }

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