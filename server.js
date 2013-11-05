//Check tutorials to learn how to start a simple node.js server
//copy and paste the following into a new js file (eg: server.js)
var restify = require('restify');
var request = require('request');

/**
* Return a timestamp with the format "m/d/yy h:MM:ss TT"
* @type {Date}
*/

function timestamp()
{
var d = new Date();
var month = d.getMonth() + 1,
date = d.getDate(),
year = d.getFullYear(),
hour = d.getHours()-2,
min = d.getMinutes(),
sec = d.getSeconds();

if(month < 10)
month = '0' + month;
if(date < 10)
date = '0' + date;

if(hour < 10)
hour = '0' + hour;
if(min < 10)
min = '0' + min;
if(sec < 10)
sec = '0' + sec;

return month + '-' + date + '-' + year + " " + hour + ":" + min + ":" + sec;
}

function respond(req, res, next) {
res.send('hello ' + req.params.name);
}

function forward(req,res,next){
res.send('hello '+JSON.stringify(req.body));
var tmpObj = JSON.parse(JSON.stringify(req.body));
tmpObj.time = timestamp() ;
var id = tmpObj.id;
delete tmpObj.id;
request.post({
headers: {'content-type' : 'application/json'},
url: 'https://ridekeepr.firebaseio.com/cases/' + id + '/map.json',
body: JSON.stringify(tmpObj)
}, function(error, response, body){
console.log(body);
});
}
var server = restify.createServer();
server.use(restify.bodyParser({ mapParams: false }));

server.get('/hello/:name', respond);
server.head('/hello/:name', respond);
server.post('/update', forward);

server.listen(8080, function() {
console.log('%s listening at %s', server.name, server.url);
});
//END THE COPY/PASTE step
/*
 * do "npm install restify" if it's missing that dependency
 * and "npm install" afterwards to install any other missing files
 */
