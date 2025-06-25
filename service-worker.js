self.addEventListener('install', event => {
  event.waitUntil(
    caches.open('qdc-cache').then(cache => {
      return cache.addAll([
        'index.html',
        'menu_v2.html',
        'menu_v2_navigation_en.html',
        'splash.html',
        'splash_screen_en.html',
        'offline.html'
      ]);
    })
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    fetch(event.request).catch(() =>
      caches.match(event.request).then(response => response || caches.match('offline.html'))
    )
  );
});