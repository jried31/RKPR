//Check tutorials to learn how to start a simple node.js server
//copy and paste the following into a new js file (eg: server.js)
var restify = require('restify');
var request = require('request');
var Kaiseki = require('kaiseki');
// instantiate
var APP_ID = 'OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f';
var REST_API_KEY = 'bPlqPguhK51mbRXaYcfnf73uTri07sk6uB64ZdPb';
var kaiseki = new Kaiseki(APP_ID, REST_API_KEY);


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

function updateParse(req,res,next){
  console.log('Got Data %s \n',req.body.id);
  var tmpObj = req.body;
  var position = {location: {
    __type: 'GeoPoint',
    latitude: parseFloat(tmpObj.lat),
    longitude: parseFloat(tmpObj.lng)
    }
  };
  
  kaiseki.updateObject('Vehicle', tmpObj.id,
                       {'AlertLevel': parseInt(tmpObj.alertLevel), 
                        'pos': position.location},
              function(err, res, body, success) {
                       console.log(body.error);
              }
   );
}

function forward(req,res,next){
res.send(JSON.stringify(req.body));
console.log('Got Data \n %s',req.body);
var tmpObj = JSON.parse(JSON.stringify(req.body));
tmpObj.time = timestamp() ;
var id = tmpObj.id;
delete tmpObj.id;
request.post({
headers: {'User-Agent' : 'curl/7.26.0','X-Parse-Application-Id' : 'OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f','X-Parse-REST-API-Key' : 'bPlqPguhK51mbRXaYcfnf73uTri07sk6uB64ZdPb','content-type' : 'application/json'},
url: '107.22.188.163:8080/1/classes/Vehicle/bht5oha6ix',
body: JSON.stringify(tmpObj)
}, function(error, response, body){
console.log(body);
});
}
var server = restify.createServer();
server.use(restify.bodyParser({ mapParams: false }));

server.post('/update', updateParse);
server.post('/forward', forward);

server.listen(8080, function() {
console.log('%s listening at %s', server.name, server.url);
});
//END THE COPY/PASTE step
/*
 * do "npm install restify" if it's missing that dependency
 * and "npm install" afterwards to install any other missing files
 */
