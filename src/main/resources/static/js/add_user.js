$(document).ready(function () {

    $('#saveUser').on('click', function () {
        var login = $('#login').val();
        var password = $('#password').val();
        var fio = $('#fio').val();
        var id = $(this).attr('name');
        var btn = $(this);
        var permission = Array();
        var checkBoxes = $('input[type=checkbox]');

        for (let i = 0; i < checkBoxes.length; i++) {
            if ($(checkBoxes[i]).is(":checked")) {
                permission.push($(checkBoxes[i]).attr('id'));
            }
        }

        if (login.length === 0) {
            alert("Необходимо указать логин");
            return;
        }

        if (password.length === 0 && id.length === 0) {
            alert("Необходимо указать пароль");
            return;
        }

        if (fio.length === 0) {
            alert("Необходимо указать ФИО");
            return;
        }

        if (permission.length === 0) {
            alert("Необходимо указать права доступа");
            return;
        }

        btn.attr('disabled', 'disabled');

        $.ajax({
            url: id.length === 0 ? '/users' : '/users/' + id,
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            cache: false,
            data: JSON.stringify({
                "fio": fio,
                "login": login,
                "password": password,
                "permissions": permission
            }),
            type: id.length === 0 ? 'POST' : 'PUT',
            error: function (e) {
                if (e.status !== 200) {
                    alert(e.responseText);
                    $('#saveUser').removeAttr('disabled');
                } else {
                    if (e.responseText === login) {
                        var xmlHttp = new XMLHttpRequest();
                        xmlHttp.open("GET", '/logout', false);
                        xmlHttp.send(null);
                        window.location.replace("/login");
                    } else {
                        window.location.replace("/users");
                    }
                }
            }
        });
    });
});