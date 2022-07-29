// Service worker.

'use strict';

console.log('SW log: Started', self);

self.addEventListener('install', function (event) {
    self.skipWaiting();
    console.log('SW log: Installed', event); // Not work ???
});

self.addEventListener('activate', function (event) {
    console.log('SW log: Activated', event); // Not work ???
});

self.addEventListener('push', function (event) {
    console.log('SW log: Push message', event); // Not work ???

    var data = event.data.text();
    console.log('SW log: Push data: ' + data); // Not work ???

    try {
        JSON.parse(data);
        var title = data.title;
        var message = data.message;
    } catch (e) {
        var title = 'Push Message';
        var message = data;
    }

    return self.registration.showNotification(title, {
        body: message,
        requireInteraction: true
    });
});
