var request = require('superagent');

module.exports = {
	getData: function(query, cb) {
		request
			.get('/uclee-user-web/getAllProduct')
			.query(query)
			.end(function(err, res) {
				if (err) {
					return err;
				}

				cb(JSON.parse(res.text));
			});
	}
};