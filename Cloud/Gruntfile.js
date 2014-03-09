/**
 * Gruntfile.js
 *
 */
module.exports = function (grunt) {

    "use strict";

    require('load-grunt-tasks')(grunt);

    grunt.initConfig({

        pkg: grunt.file.readJSON('package.json'),

        jshint: {
            options: {
                "indent": 4,
                "browser": true,
                "strict": true,
                "newcap": true,
                "undef": true,
                "curly": true,
                "eqeqeq": true,
                "immed": true,
                "latedef": true,
                "noarg": true,
                "sub": true,
                "boss": true,
                "eqnull": true,
                "laxcomma": true,
                "laxbreak": true,
                // put all globals here
                "globals": {
                    "require": false,
                    "module": false,
                    "console": false,
                    "exports": false,

                    // mocha test framework
                    "describe": false,
                    "it": false
                }
            },
            all: {
                src: [
                    "Gruntfile.js","*.js", "test/**/*.js"
                ]
            }
        },
        jsonlint: {
            all: {
                src: ["*.json"]
            }
        }

    });

    grunt.registerTask('default',  ['jsonlint','jshint']);

};
