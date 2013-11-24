var restify 		= require('restify');
var request 		= require('request');
var kaiseki_inc 	= require('kaiseki');

// instantiate
var APP_ID = 'OZzFan5hpI4LoIqfd8nAJZDFZ3ZLJ70ZvkYCNJ6f';
var REST_API_KEY = 'bPlqPguhK51mbRXaYcfnf73uTri07sk6uB64ZdPb';
var kaiseki = new kaiseki_inc(APP_ID, REST_API_KEY);


/**
* Return a timestamp with the format "m/d/yy h:MM:ss TT"
* @type {Date}
*/

function timestamp()
{
	var d 		= new Date();
	var month 	= d.getMonth() + 1,
	var date 	= d.getDate(),
	var year 	= d.getFullYear(),
	var hour 	= d.getHours() - 2 ,
	var min 	= d.getMinutes(),
	var sec 	= d.getSeconds();

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

/**
 * updateVehicleStatus
 *
 * Updates the status of a vehicle. Generally called by the GeogramONE, using
 * information read from the sensor.
 *
 * @param string 	id 				Vehicle ID
 * @param int 		alertLevel 		New alertLevel (based on ambigious alert leveling...)
 * @param GeoPoint 	location		Vehicle location
 */
function updateVehicleStatus(req,res,next){
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
var server = restify.createServer();
server.use(restify.bodyParser({ mapParams: false }));

server.post('/update', updateVehicleStatus);

server.listen(8080, function() {
	console.log('%s listening at %s', server.name, server.url);
});
/*
 * do "npm install restify" if it's missing that dependency
 * and "npm install" afterwards to install any other missing files
 */
