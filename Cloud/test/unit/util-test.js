/* jshint -W097 */

"use strict";

var expect = require('expect.js'),
    util = require('../../util.js'),
    timestamp = util.timestamp;

describe('Util', function() {

    describe('#timestamp', function() {
        it('should be formatted as mm-dd-yyyy hh:MM:ss', function(){

            var datePattern = /(\d){2}-(\d){2}-(\d){4} (\d){2}:(\d){2}:(\d){2}/;
            expect( datePattern.test(timestamp()) ).to.be(true);

        });
    });

});

