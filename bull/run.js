var server = require('http').createServer()
  , Queue = require('bull')
  , request = require('request')
  , express = require('express')
  , app = express()
  , port = process.env.PORT || 8080
  , env = process.env.ENV || 'prod'
  , redis = process.env.REDIS || 'redis://redis:6379'
  , PST_API = process.env.PST_API || 'http://pst-service:8888'
  , BLEVE_API = process.env.BLEVE_API || 'http://bleve-explorer:8000/'
  ;

var uploadPst = function (req) {
  return req.pipe(request(PST_API+request.originalUrl));
}

var indexPst = function (parsedPstJson) {
  return request(BLEVE_API + '_bulk/' + parsedPstJson.name + '/messageId', parsedPstJson.emails)
}

var pstQueue = new Queue('pst parsing', redis);
var indexQueue = new Queue('pst indexing', redis);

indexQueue.process(function (job, done) {
  console.log('INDEXING JOB: ', job.id);
  if (job.name && job.emails) {
    let url = [BLEVE_API, '_bulk', job.name, 'messageId'].join('/');
    return request.post(url, job.emails)
  }
  done();
});

pstQueue.process(function (job, done) {
  console.log('PARSE PST JOB: ', job.id);
  return job.request.pipe(request(PST_API+request.originalUrl));
});

app.post('/upload', function (req, res) {
  return pstQueue.add({ request: req });
})

app.get('/status/:id', function (req, res) {
  if (req.params.id) {
    res.send(req.params.id);
  } else {
    res.send('Invalid Job Id!');
  }
})

server.on('request', app);
server.listen(port, function() { console.log('Listening on ' + server.address().port) });