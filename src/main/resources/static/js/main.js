console.log('JavaScript loaded...');
function testUserMail (username) {
    let data = new FormData()
    data.append( "username", username );
    fetch('/admin/users/mail-test',
        {
            credentials: 'include',
            method: 'POST',
            body: data
        })
        .then((response) => {
            WebPushService.infoMessage(response);
            return response.json();
        })
        .then((data) => {
            if(data != 200) {
                WebPushService.errorMessage(data);
                alert('Failed !!!');
            } else {
                alert('OK !!!');
            }
        });
}
