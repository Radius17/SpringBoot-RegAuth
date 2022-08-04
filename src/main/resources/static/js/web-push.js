var WebPushService = {
    register: function () {
        if(typeof(WebPushServiceKey)=='undefined'){
            WebPushService.warningMessage("Public key not found !!!");
            return;
        }
        if ('serviceWorker' in navigator) {
            navigator.serviceWorker
                .register('/web-push-sw.js')
                .then(WebPushService.initWorker())
                .catch(function (err) {
                    WebPushService.warningMessage('Error while service worker registration !!!', err);
                });
        } else {
            WebPushService.warningMessage('Service workers are not supported in this browser !!!');
        }
    },
    initWorker: function () {
        if (!('showNotification' in ServiceWorkerRegistration.prototype)) {
            WebPushService.warningMessage('Notifications are not supported !!!');
            return;
        }
        if (!('PushManager' in window)) {
            WebPushService.warningMessage('Push messages are not supported !!!');
            return;
        }
        if (Notification.permission === 'denied') {
            WebPushService.warningMessage('User blocked notifications !!!');
            return;
        }

        WebPushService.infoMessage('Service worker is initialising...');

        navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {
            WebPushService.infoMessage('Service worker is ready...');
            serviceWorkerRegistration.pushManager.getSubscription()
                .then(function (subscription) {
                    if (!subscription) {
                        WebPushService.proceedSubscription();
                    } else {
                        WebPushService.infoMessage('Refreshing subscription...');
                        WebPushService.sendSubscription(subscription);
                    }
                })
                .catch(function (err) {
                    WebPushService.warningMessage('Error while subscription process !!!', err);
                });
        });
    },
    proceedSubscription: function () {
        WebPushService.infoMessage('Let\'s try to subscribe...');
        navigator.serviceWorker.ready.then(function (serviceWorkerRegistration) {
            serviceWorkerRegistration.pushManager
                .subscribe({
                    applicationServerKey: WebPushServiceKey,
                    userVisibleOnly: true
                })
                .then(function (subscription) {
                    return WebPushService.sendSubscription(subscription);
                })
                .catch(function (e) {
                    if (Notification.permission === 'denied') {
                        WebPushService.errorMessage('Permission for notifications denied !!!');
                    } else {
                        WebPushService.errorMessage('Unable to subscribe to push messages !!!', e);
                    }
                });
        });
    },
    sendSubscription: function (subscription) {
        WebPushService.infoMessage('Let\'s send subscription to server...');
        // WebPushService.warningMessage('Subscription details:', subscription);

        let jsonSubscription = {
            endpoint: subscription.endpoint,
            keys: {
                p256dh: subscription.getKey ? btoa(String.fromCharCode.apply(null, new Uint8Array(subscription.getKey('p256dh')))) : '',
                auth: subscription.getKey ? btoa(String.fromCharCode.apply(null, new Uint8Array(subscription.getKey('auth')))) : ''
            }
        };
        // WebPushService.warningMessage('JSON Subscription details:', jsonSubscription);

        return fetch('/profile/subscribe', {
            credentials: 'include',
            headers: {'Content-Type': 'application/json'},
            method: 'POST',
            body: JSON.stringify(jsonSubscription)
        });
    },
    testMySubscription: function () {
        fetch('/profile/subscribe-test' )
        .then((response) => {
            WebPushService.infoMessage(response);
            return response.json();
        })
        .then((data) => {
            if(data != 201) {
                WebPushService.errorMessage(data);
                alert('Failed !!!');
            } else {
                alert('OK !!!');
            }
        });
    },
    testUserSubscription: function (username) {
        let data = new FormData()
        data.append( "username", username );
        fetch('/admin/users/subscribe-test',
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
                if(data != 201) {
                    WebPushService.errorMessage(data);
                    alert('Failed !!!');
                } else {
                    alert('OK !!!');
                }
            });
    },
    infoMessage: function (mess, obj) {
        if(typeof(obj) == 'undefined' ) console.log(mess);
        else console.log(mess, obj);
    },
    warningMessage: function (mess, obj) {
        if(typeof(obj) == 'undefined' ) console.warn(mess);
        else console.warn(mess, obj);
    },
    errorMessage: function (mess) {
        if(typeof(obj) == 'undefined' ) console.error(mess);
        else console.error(mess, obj);
    }
};

window.addEventListener('load', WebPushService.register, false);
