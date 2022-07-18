$(document).ready(function () {

    $('#btn-lock').on('click', function () {
        var id = $(this).attr('name');
        var btn = $(this);

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/users/' + id + '/lock',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            type: 'PUT',
            error: function (e) {
                if (e.status !== 200) {
                    alert(e.responseText);
                    $('#btn-lock').removeAttr('disabled');
                } else {
                    window.location.reload();
                }
            }
        });
    });

    $('#btn-unlock').on('click', function () {
        var id = $(this).attr('name');
        var btn = $(this);

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: '/users/' + id + '/unlock',
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            type: 'PUT',
            error: function (e) {
                if (e.status !== 200) {
                    alert(e.responseText);
                    $('#btn-unlock').removeAttr('disabled');
                } else {
                    window.location.reload();
                }
            }
        });
    });
});