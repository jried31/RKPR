/* jshint -W097 */
"use strict";
/**
 * @overview Utility Methods
 */

/**
 * @function timestamp
 * @desc Returns a timestamp with the format 'mm-dd-yyyy hh:MM:ss'
 * @return {String} the generated timestamp
 */
function timestamp() {

    var d       = new Date(),
        month   = d.getMonth() + 1,
        date    = d.getDate(),
        year    = d.getFullYear(),
        hour    = d.getHours() - 2,
        min     = d.getMinutes(),
        sec     = d.getSeconds();

    // append 0 to single digit numbers
    if (month < 10) {
        month = '0' + month;
    }
    if (date < 10) {
        date = '0' + date;
    }
    if (hour < 10) {
        hour = '0' + hour;
    }
    if (min < 10) {
        min = '0' + min;
    }
    if (sec < 10) {
        sec = '0' + sec;
    }

    return month + '-' + date + '-' + year + ' ' + hour + ':' + min + ':' + sec;
}

/**
 * @function diffTime
 *
 * @param {String} end - the ending timestamp
 * @param {String} start - the starting timestamp
 *
 * @return {Number} the difference in milliseconds
 */
function diffTime(end, begin) {
    return Date.parse(end) - Date.parse(begin);
}

module.exports = {
    timestamp: timestamp,
    diffTime: diffTime
};
