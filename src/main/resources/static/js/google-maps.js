(function () {
    'use strict';

    var DEFAULT_LAT = 7.8731;
    var DEFAULT_LNG = 80.7718;
    var DEFAULT_ZOOM = 7;
    var PIN_ZOOM = 16;
    var googleLoaded = false;
    var initialized = false;

    function byId(id) {
        return document.getElementById(id);
    }

    function validNumber(value) {
        return value !== null && value !== undefined && value !== '' && !isNaN(parseFloat(value));
    }

    function hasCoordinates(latInput, lngInput) {
        return !!latInput && !!lngInput && validNumber(latInput.value) && validNumber(lngInput.value);
    }

    function asFixed(value) {
        var n = parseFloat(value);
        return isNaN(n) ? '' : n.toFixed(6);
    }

    function encode(value) {
        return encodeURIComponent(value || 'Sri Lanka');
    }

    function coordinatesQuery(latInput, lngInput) {
        return latInput.value + ',' + lngInput.value;
    }

    function locationFromInputs(addressInput, cityInput) {
        var address = addressInput ? addressInput.value || '' : '';
        var city = cityInput ? cityInput.value || '' : '';
        var q = (address + ' ' + city).trim();
        return q || 'Sri Lanka';
    }

    function googleEmbedUrl(query) {
        return 'https://maps.google.com/maps?q=' + encode(query) + '&output=embed';
    }

    function showFallback(mapDiv, message) {
        if (!mapDiv) return;
        mapDiv.innerHTML = '<div class="map-fallback">' + message + '</div>';
    }

    function renderNoKeyGoogleMap(mapDiv, frameId, query, note) {
        if (!mapDiv) return;
        mapDiv.innerHTML =
            '<iframe id="' + frameId + '" class="no-key-google-map" loading="lazy" src="' + googleEmbedUrl(query) + '"></iframe>' +
            '<div class="no-key-map-note">' + note + '</div>';
    }

    function updateFallbackFrame(frameId, query) {
        var frame = byId(frameId);
        if (frame) frame.src = googleEmbedUrl(query);
    }

    function missingKey() {
        var meta = document.querySelector('meta[name="google-maps-api-key"]');
        var key = meta ? (meta.getAttribute('content') || '').trim() : '';
        return !key || key === 'YOUR_GOOGLE_MAPS_API_KEY';
    }

    function getApiKey() {
        var meta = document.querySelector('meta[name="google-maps-api-key"]');
        return meta ? (meta.getAttribute('content') || '').trim() : '';
    }

    function makeMarker(map, position, onDragEnd) {
        return new google.maps.Marker({
            map: map,
            position: position,
            draggable: true,
            animation: google.maps.Animation.DROP
        });
    }

    function updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput) {
        var mapFrame = byId('storeMap');
        var open = byId('openStoreMap');
        var coord = byId('coordText');
        var hasPin = hasCoordinates(latInput, lngInput);
        var query = hasPin ? coordinatesQuery(latInput, lngInput) : locationFromInputs(addressInput, cityInput);
        var encoded = encode(query);

        if (mapFrame) mapFrame.src = googleEmbedUrl(query);
        updateFallbackFrame('storePinFallbackFrame', query);
        if (open) open.href = 'https://www.google.com/maps/search/?api=1&query=' + encoded;
        if (coord) {
            coord.textContent = hasPin
                ? 'Saved store pin: ' + latInput.value + ', ' + lngInput.value
                : 'No GPS pin saved. Google preview is using the typed address.';
        }
    }

    function updateDeliveryRoute(latInput, lngInput, addressInput, cityInput) {
        var route = byId('routeMap');
        var routeLink = byId('openRouteLink');
        var coord = byId('deliveryCoordText');
        var store = route && route.dataset.storeQuery ? route.dataset.storeQuery : 'Sri Lanka';
        var destination = hasCoordinates(latInput, lngInput)
            ? coordinatesQuery(latInput, lngInput)
            : locationFromInputs(addressInput, cityInput);

        if (route) route.src = 'https://maps.google.com/maps?saddr=' + encode(store) + '&daddr=' + encode(destination) + '&output=embed';
        updateFallbackFrame('deliveryPinFallbackFrame', destination);
        if (routeLink) routeLink.href = 'https://www.google.com/maps/dir/?api=1&origin=' + encode(store) + '&destination=' + encode(destination);
        if (coord) {
            coord.textContent = hasCoordinates(latInput, lngInput)
                ? 'Saved delivery pin: ' + latInput.value + ', ' + lngInput.value + ' — route updated automatically.'
                : 'No GPS pin saved. Route is using the typed address.';
        }
    }

    function setStorePin(state, latValue, lngValue, zoom) {
        var la = parseFloat(latValue);
        var lo = parseFloat(lngValue);
        if (isNaN(la) || isNaN(lo)) return;

        state.latInput.value = asFixed(la);
        state.lngInput.value = asFixed(lo);
        var position = { lat: la, lng: lo };

        if (!state.marker) {
            state.marker = makeMarker(state.map, position);
            state.marker.addListener('dragend', function () {
                var p = state.marker.getPosition();
                setStorePin(state, p.lat(), p.lng(), state.map.getZoom());
            });
        } else {
            state.marker.setMap(state.map);
            state.marker.setPosition(position);
        }

        state.map.setCenter(position);
        state.map.setZoom(zoom || Math.max(state.map.getZoom(), PIN_ZOOM));
        updateGoogleStorePreview(state.latInput, state.lngInput, state.addressInput, state.cityInput);
    }

    function initStoreMap() {
        var mapDiv = byId('storePinMap');
        if (!mapDiv) return;

        var latInput = byId('latitude');
        var lngInput = byId('longitude');
        var addressInput = byId('address');
        var cityInput = byId('city');
        var addPinBtn = byId('addStorePin');
        var clearBtn = byId('clearStorePin');
        var locationBtn = byId('useStoreLocation');
        var timer = null;

        var startLat = hasCoordinates(latInput, lngInput) ? parseFloat(latInput.value) : DEFAULT_LAT;
        var startLng = hasCoordinates(latInput, lngInput) ? parseFloat(lngInput.value) : DEFAULT_LNG;
        var state = {
            map: new google.maps.Map(mapDiv, {
                center: { lat: startLat, lng: startLng },
                zoom: hasCoordinates(latInput, lngInput) ? PIN_ZOOM : DEFAULT_ZOOM,
                mapTypeControl: false,
                streetViewControl: false,
                fullscreenControl: true
            }),
            marker: null,
            latInput: latInput,
            lngInput: lngInput,
            addressInput: addressInput,
            cityInput: cityInput
        };

        if (hasCoordinates(latInput, lngInput)) {
            setStorePin(state, latInput.value, lngInput.value, PIN_ZOOM);
        }

        state.map.addListener('click', function (event) {
            setStorePin(state, event.latLng.lat(), event.latLng.lng(), PIN_ZOOM);
        });

        if (addPinBtn) {
            addPinBtn.addEventListener('click', function () {
                if (!hasCoordinates(latInput, lngInput)) {
                    alert('Please click the map or enter both latitude and longitude first.');
                    return;
                }
                setStorePin(state, latInput.value, lngInput.value, PIN_ZOOM);
            });
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', function () {
                latInput.value = '';
                lngInput.value = '';
                if (state.marker) state.marker.setMap(null);
                updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (locationBtn) {
            locationBtn.addEventListener('click', function () {
                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by this browser.');
                    return;
                }
                navigator.geolocation.getCurrentPosition(function (pos) {
                    setStorePin(state, pos.coords.latitude, pos.coords.longitude, PIN_ZOOM);
                }, function () {
                    alert('Unable to read your location. Please allow location access or type latitude and longitude manually.');
                });
            });
        }

        function debouncedPreview() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                if (!hasCoordinates(latInput, lngInput)) updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
            }, 500);
        }

        if (addressInput) addressInput.addEventListener('input', debouncedPreview);
        if (cityInput) cityInput.addEventListener('input', debouncedPreview);
        updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
    }

    function setDeliveryPin(state, latValue, lngValue, zoom) {
        var la = parseFloat(latValue);
        var lo = parseFloat(lngValue);
        if (isNaN(la) || isNaN(lo)) return;

        state.latInput.value = asFixed(la);
        state.lngInput.value = asFixed(lo);
        var position = { lat: la, lng: lo };

        if (!state.marker) {
            state.marker = makeMarker(state.map, position);
            state.marker.addListener('dragend', function () {
                var p = state.marker.getPosition();
                setDeliveryPin(state, p.lat(), p.lng(), state.map.getZoom());
            });
        } else {
            state.marker.setMap(state.map);
            state.marker.setPosition(position);
        }

        state.map.setCenter(position);
        state.map.setZoom(zoom || Math.max(state.map.getZoom(), PIN_ZOOM));
        if (state.addressInput && !state.addressInput.value) {
            state.addressInput.value = 'Pinned delivery location: ' + state.latInput.value + ', ' + state.lngInput.value;
        }
        updateDeliveryRoute(state.latInput, state.lngInput, state.addressInput, state.cityInput);
        drawRouteOnDeliveryMap(state);
    }

    function drawRouteOnDeliveryMap(state) {
        var route = byId('routeMap');
        var store = route && route.dataset.storeQuery ? route.dataset.storeQuery : 'Sri Lanka';
        if (!state.directionsService || !state.directionsRenderer) return;
        if (!hasCoordinates(state.latInput, state.lngInput)) return;

        state.directionsService.route({
            origin: store,
            destination: coordinatesQuery(state.latInput, state.lngInput),
            travelMode: google.maps.TravelMode.DRIVING
        }, function (result, status) {
            if (status === 'OK') {
                state.directionsRenderer.setDirections(result);
            }
        });
    }

    function initDeliveryMap() {
        var mapDiv = byId('deliveryPinMap');
        if (!mapDiv) return;

        var latInput = byId('customerLatitude');
        var lngInput = byId('customerLongitude');
        var addressInput = byId('deliveryAddress');
        var cityInput = byId('deliveryCity');
        var addPinBtn = byId('addDeliveryPin');
        var typedBtn = byId('useTypedDeliveryAddress');
        var locationBtn = byId('useDeliveryLocation');
        var timer = null;

        var startLat = hasCoordinates(latInput, lngInput) ? parseFloat(latInput.value) : DEFAULT_LAT;
        var startLng = hasCoordinates(latInput, lngInput) ? parseFloat(lngInput.value) : DEFAULT_LNG;
        var state = {
            map: new google.maps.Map(mapDiv, {
                center: { lat: startLat, lng: startLng },
                zoom: hasCoordinates(latInput, lngInput) ? PIN_ZOOM : DEFAULT_ZOOM,
                mapTypeControl: false,
                streetViewControl: false,
                fullscreenControl: true
            }),
            marker: null,
            latInput: latInput,
            lngInput: lngInput,
            addressInput: addressInput,
            cityInput: cityInput,
            directionsService: new google.maps.DirectionsService(),
            directionsRenderer: new google.maps.DirectionsRenderer({ suppressMarkers: false })
        };
        state.directionsRenderer.setMap(state.map);

        if (hasCoordinates(latInput, lngInput)) {
            setDeliveryPin(state, latInput.value, lngInput.value, PIN_ZOOM);
        }

        state.map.addListener('click', function (event) {
            setDeliveryPin(state, event.latLng.lat(), event.latLng.lng(), PIN_ZOOM);
        });

        if (addPinBtn) {
            addPinBtn.addEventListener('click', function () {
                if (!hasCoordinates(latInput, lngInput)) {
                    alert('Please click the map or enter both latitude and longitude first.');
                    return;
                }
                setDeliveryPin(state, latInput.value, lngInput.value, PIN_ZOOM);
            });
        }

        if (typedBtn) {
            typedBtn.addEventListener('click', function () {
                latInput.value = '';
                lngInput.value = '';
                if (state.marker) state.marker.setMap(null);
                state.directionsRenderer.set('directions', null);
                updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (locationBtn) {
            locationBtn.addEventListener('click', function () {
                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by this browser.');
                    return;
                }
                navigator.geolocation.getCurrentPosition(function (pos) {
                    setDeliveryPin(state, pos.coords.latitude, pos.coords.longitude, PIN_ZOOM);
                    if (cityInput && !cityInput.value) cityInput.value = 'Current location';
                }, function () {
                    alert('Unable to read your location. Please allow location access or type latitude and longitude manually.');
                });
            });
        }

        function debouncedRoute() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                if (!hasCoordinates(latInput, lngInput)) updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
            }, 500);
        }

        if (addressInput) addressInput.addEventListener('input', debouncedRoute);
        if (cityInput) cityInput.addEventListener('input', debouncedRoute);
        updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
    }

    function initMaps() {
        if (initialized) return;
        initialized = true;
        initStoreMap();
        initDeliveryMap();
    }


    function bindStoreFallback() {
        var latInput = byId('latitude');
        var lngInput = byId('longitude');
        var addressInput = byId('address');
        var cityInput = byId('city');
        var addPinBtn = byId('addStorePin');
        var clearBtn = byId('clearStorePin');
        var locationBtn = byId('useStoreLocation');
        var timer = null;

        if (!latInput || !lngInput) return;

        if (addPinBtn && !addPinBtn.dataset.googleFallbackBound) {
            addPinBtn.dataset.googleFallbackBound = 'true';
            addPinBtn.addEventListener('click', function () {
                if (!hasCoordinates(latInput, lngInput)) {
                    alert('Please enter both latitude and longitude first.');
                    return;
                }
                latInput.value = asFixed(latInput.value);
                lngInput.value = asFixed(lngInput.value);
                updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (clearBtn && !clearBtn.dataset.googleFallbackBound) {
            clearBtn.dataset.googleFallbackBound = 'true';
            clearBtn.addEventListener('click', function () {
                latInput.value = '';
                lngInput.value = '';
                updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (locationBtn && !locationBtn.dataset.googleFallbackBound) {
            locationBtn.dataset.googleFallbackBound = 'true';
            locationBtn.addEventListener('click', function () {
                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by this browser.');
                    return;
                }
                navigator.geolocation.getCurrentPosition(function (pos) {
                    latInput.value = asFixed(pos.coords.latitude);
                    lngInput.value = asFixed(pos.coords.longitude);
                    updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
                }, function () {
                    alert('Unable to read your location. Please allow location access or type latitude and longitude manually.');
                });
            });
        }

        function debouncedPreview() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                if (!hasCoordinates(latInput, lngInput)) updateGoogleStorePreview(latInput, lngInput, addressInput, cityInput);
            }, 500);
        }

        if (addressInput && !addressInput.dataset.googleFallbackBound) {
            addressInput.dataset.googleFallbackBound = 'true';
            addressInput.addEventListener('input', debouncedPreview);
        }
        if (cityInput && !cityInput.dataset.googleFallbackBound) {
            cityInput.dataset.googleFallbackBound = 'true';
            cityInput.addEventListener('input', debouncedPreview);
        }
    }

    function bindDeliveryFallback() {
        var latInput = byId('customerLatitude');
        var lngInput = byId('customerLongitude');
        var addressInput = byId('deliveryAddress');
        var cityInput = byId('deliveryCity');
        var addPinBtn = byId('addDeliveryPin');
        var typedBtn = byId('useTypedDeliveryAddress');
        var locationBtn = byId('useDeliveryLocation');
        var timer = null;

        if (!latInput || !lngInput) return;

        if (addPinBtn && !addPinBtn.dataset.googleFallbackBound) {
            addPinBtn.dataset.googleFallbackBound = 'true';
            addPinBtn.addEventListener('click', function () {
                if (!hasCoordinates(latInput, lngInput)) {
                    alert('Please enter both latitude and longitude first.');
                    return;
                }
                latInput.value = asFixed(latInput.value);
                lngInput.value = asFixed(lngInput.value);
                if (addressInput && !addressInput.value) {
                    addressInput.value = 'Pinned delivery location: ' + latInput.value + ', ' + lngInput.value;
                }
                updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (typedBtn && !typedBtn.dataset.googleFallbackBound) {
            typedBtn.dataset.googleFallbackBound = 'true';
            typedBtn.addEventListener('click', function () {
                latInput.value = '';
                lngInput.value = '';
                updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
            });
        }

        if (locationBtn && !locationBtn.dataset.googleFallbackBound) {
            locationBtn.dataset.googleFallbackBound = 'true';
            locationBtn.addEventListener('click', function () {
                if (!navigator.geolocation) {
                    alert('Geolocation is not supported by this browser.');
                    return;
                }
                navigator.geolocation.getCurrentPosition(function (pos) {
                    latInput.value = asFixed(pos.coords.latitude);
                    lngInput.value = asFixed(pos.coords.longitude);
                    if (addressInput && !addressInput.value) {
                        addressInput.value = 'Pinned delivery location: ' + latInput.value + ', ' + lngInput.value;
                    }
                    if (cityInput && !cityInput.value) cityInput.value = 'Current location';
                    updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
                }, function () {
                    alert('Unable to read your location. Please allow location access or type latitude and longitude manually.');
                });
            });
        }

        function debouncedRoute() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                if (!hasCoordinates(latInput, lngInput)) updateDeliveryRoute(latInput, lngInput, addressInput, cityInput);
            }, 500);
        }

        if (addressInput && !addressInput.dataset.googleFallbackBound) {
            addressInput.dataset.googleFallbackBound = 'true';
            addressInput.addEventListener('input', debouncedRoute);
        }
        if (cityInput && !cityInput.dataset.googleFallbackBound) {
            cityInput.dataset.googleFallbackBound = 'true';
            cityInput.addEventListener('input', debouncedRoute);
        }
    }

    function initFallbacks(message) {
        var storeLat = byId('latitude');
        var storeLng = byId('longitude');
        var storeAddress = byId('address');
        var storeCity = byId('city');
        var storeQuery = (storeLat && storeLng && hasCoordinates(storeLat, storeLng))
            ? coordinatesQuery(storeLat, storeLng)
            : locationFromInputs(storeAddress, storeCity);

        var deliveryLat = byId('customerLatitude');
        var deliveryLng = byId('customerLongitude');
        var deliveryAddress = byId('deliveryAddress');
        var deliveryCity = byId('deliveryCity');
        var deliveryQuery = (deliveryLat && deliveryLng && hasCoordinates(deliveryLat, deliveryLng))
            ? coordinatesQuery(deliveryLat, deliveryLng)
            : locationFromInputs(deliveryAddress, deliveryCity);

        renderNoKeyGoogleMap(
            byId('storePinMap'),
            'storePinFallbackFrame',
            storeQuery,
            'Google Maps preview. Use current location or type latitude/longitude, then press Add this location.'
        );
        renderNoKeyGoogleMap(
            byId('deliveryPinMap'),
            'deliveryPinFallbackFrame',
            deliveryQuery,
            'Google Maps preview. Use current location or type latitude/longitude, then press Add this location.'
        );

        bindStoreFallback();
        bindDeliveryFallback();
        if (storeLat && storeLng) updateGoogleStorePreview(storeLat, storeLng, storeAddress, storeCity);
        if (deliveryLat && deliveryLng) updateDeliveryRoute(deliveryLat, deliveryLng, deliveryAddress, deliveryCity);
    }

    window.initFoodieGoGoogleMaps = function () {
        googleLoaded = true;
        initMaps();
    };

    function loadGoogleMaps() {
        if (!byId('storePinMap') && !byId('deliveryPinMap')) return;

        if (missingKey()) {
            initFallbacks('Google Maps JavaScript key is not configured. The app is using a no-key Google Maps preview with manual/current-location saving.');
            return;
        }

        if (window.google && window.google.maps) {
            window.initFoodieGoGoogleMaps();
            return;
        }

        var script = document.createElement('script');
        script.src = 'https://maps.googleapis.com/maps/api/js?key=' + encodeURIComponent(getApiKey()) + '&callback=initFoodieGoGoogleMaps&loading=async';
        script.async = true;
        script.defer = true;
        script.onerror = function () {
            initFallbacks('Google Maps JavaScript could not load. The app is using a no-key Google Maps preview with manual/current-location saving.');
        };
        document.head.appendChild(script);

        setTimeout(function () {
            if (!googleLoaded) {
                initFallbacks('Google Maps JavaScript is still loading or blocked. The app is using a no-key Google Maps preview with manual/current-location saving.');
            }
        }, 8000);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', loadGoogleMaps);
    } else {
        loadGoogleMaps();
    }
})();
