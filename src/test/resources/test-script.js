logger.info('URL: ' + url);
logger.info('Status code: ' + response.statusCode);
logger.info('Response headers: ' + response.headers);
logger.info('Original response length: ' + (response.body ? response.body.length : 0));

// override the response body
response.body = "Hello world";
