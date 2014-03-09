/* jshint -W097 */
"use strict";
/**
 * @function timestamp
 * @desc Return a timestamp with the format 'mm-dd-yyyy hh:MM:ss'
 * @return {String} - the generated timestamp
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

module.exports = {
    timestamp: timestamp
};
