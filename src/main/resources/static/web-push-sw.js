// Service worker.

'use strict';

console.log('SW log [Started]', self);

self.addEventListener('install', function (event) {
    self.skipWaiting();
    console.log('SW log [Installed]:', event);
});

self.addEventListener('activate', function (event) {
    console.log('SW log [Activated]:', event);
});

self.addEventListener('push', function (event) {
    console.log('SW log [Pushing message]:', event);

    var data = event.data.text();
    console.log('SW log [Push data]: ' + data);

    try {
        var dataObj = JSON.parse(data);
        var title = dataObj.title;
        var message = dataObj.message;
    } catch (e) {
        console.log('SW log [JSON parse error]: ' + e);
    }

    if (typeof (title) == 'undefined') {
        var title = 'Message';
    }
    if (typeof (message) == 'undefined') {
        var message = data;
    }

    return self.registration.showNotification(title, {
        body: message,
        requireInteraction: true
    });
});
