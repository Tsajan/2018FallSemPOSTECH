const express = require('express');
const app = express();
const fs = require('fs');

var bodyParser = require('body-parser');
var mysql = require('mysql'); 
var async = require('async');

var connection = mysql.createConnection({
  multipleStatements: true,
  host: "localhost",
  user: "root",
  password: "root",
  database: "bms"
});

connection.connect();

app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));
app.set('view engine', 'ejs')

app.get('/bms', function (req, res) {

	var ethTransJSON;
	var ethBlockJSON;

	console.log("Starting Blockchain Monitoring System");

	connection.beginTransaction(function (err) {
        var eth_txn_sql = "select hash as txnhash, '10 minutes ago' as age, ethValue from ethtransaction order by hash desc limit 3";
        var eth_block_sql = "select blocknumber, date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, 12 as tnum, miner, size from ethBlock order by blockNumber desc limit 3";

        connection.query(eth_txn_sql, function (err, rows, fields) {
            if (err) {
                console.log("Error while getting data from ethTransaction");
            } else {
                console.log("Getting ethTransaction data");
                ethTransJSON = rows;
            }
        });

        connection.query(eth_block_sql, function (err, rows, fields) {
            if (err) {
                console.log("Error while getting data from ethBlock");
            } else {
                console.log("Getting ethBlock data");
                ethBlockJSON = rows;
            }
        });
        connection.commit(function (err, info) {
            console.log("commit");
        	res.render('index', { ethTransaction: ethTransJSON, ethBlock: ethBlockJSON });
        });

    });
})

app.post('/search', function(req, res) {

	var ethblockinfo;

	console.log("Search started");
	var hashaddrflg = 0;
	var inputField = req.body.searchval;
	console.log("User search query: " + inputField);
	if(inputField.startsWith('0x')) {
		console.log("Search for a hash value entered by user");
		hashaddrflg = 1;
	}
  		/*
  		if(hashaddrflg) {
  			inputField = inputField.substring(2);
  			console.log("Search hash value": + inputField);
  			var sql = 'select count(*) from ethblock where blocknumber = ?';

  			connection.query(sql, [inputField], function(err, result, fields) {
  				if(err) throw err;
  				console.log(result);
  			});
  		}
  		*/
  	if(!hashaddrflg) {
 		var blocksql = "select count(*) as cnt from ethblock where blocknumber = ?";
  		connection.query(blocksql, [inputField], function(err, result, fields) {
  			if(err) throw err;
  			console.log(result);
  			if(result[0].cnt) {
  				console.log("Matching record found");
  				var nextblocksql = "select blocknumber, miner, date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, difficulty, size, nonce, sha3uncles,  ifnull(transactions,0) as numtxn, ifnull(uncles,0) as numuncles, totaldifficulty, gasused, gaslimit, hash, parenthash from ethblock where blocknumber = ?";
  				connection.query(nextblocksql, [inputField], function(err, out, fields) {
  					if(err) throw err;
  					ethblockinfo = out;
  					console.log(out);
  					res.render('block', { ethblockinfo: ethblockinfo });
  				})
  			}
  			else {
  				console.log("No records found");
  				res.write("No records found");
  			}
  		});
  	}
  	else {
  		inputField = inputField.substr(2);
  		console.log(inputField);
  		var sql = "select count(1) as cnt from ethblock where hash = ? union select count(1) from ethtransaction where hash = ?";
  		connection.query(sql, [inputField, inputField], function(err, result, fields) {
  			if(err) throw err;
  			console.log(result);
  		});
  	}
  	
});

app.get('/eth/block', function(req, res) {
	console.log("Rendering block information page");
	//var inputBlockNum = req.body.
	res.render('block');
})

app.listen(3000, function () {
  console.log('Server running on port 3000!')
})