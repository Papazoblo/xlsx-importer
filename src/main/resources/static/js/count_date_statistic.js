$(document).ready(function () {

    $('.btn-search').on('click', function () {
        window.location.href = '/contacts/count-date-statistic?' + getJsonFilter();
    });

    function getJsonFilter() {
        var createDateFrom = formatDate($('#createDateFromFilter').val());
        var createDateTo = formatDate($('#createDateToFilter').val());

        return new URLSearchParams({
            "dateFrom": createDateFrom,
            "dateTo": createDateTo,
        }).toString()
    }

    function formatDate(dateString) {
        if (dateString !== undefined && dateString.length > 0) {
            var splitAll = dateString.split(" ");
            var splitDate = splitAll[0].split(".");
            return splitDate[2] + "-" + splitDate[1] + "-" + splitDate[0];
        }
        return "";
    }
});