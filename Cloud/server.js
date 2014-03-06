(function() {

    "use strict";

    var restify = require('restify'),
        request = require('request'),
        cloud = require('./cloud.js'),
        server = restify.createServer();

    server.use(restify.bodyParser({ mapParams: false }));

    server.post('/update', cloud.processTrackerAlert);

    server.listen(8888, function() {
        console.log('%s listening at %s', server.name, server.url);
    });

    /**
    * A 'cronjob' to run the notify task. Yeah,
    * it leaks memory.
    */

    var minutes = 1, interval = minutes * 1000 * 60;
    setInterval(cloud.notifyNearbyUsers, interval/4);

})();
