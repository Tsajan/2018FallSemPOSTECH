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
	var eth_block_heights = [];
	var eth_block_ts = [];
	var eth_block_tnums = [];
	var eth_block_miners = [];
	var eth_block_sizes = [];

	var eth_txn_txnhash = [];
	var eth_txn_age = [];
	var eth_txn_amt = [];

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
	/*
	con.connect(function(err) {
  		if (err) throw err;
  		console.log("Connected to MySQL!");

  		var eth_block_sql = "select blocknumber, ts, 12 as tnum, miner, size from ethBlock order by blockNumber desc limit 3";
  		con.query(eth_block_sql, function(err, result, fields) {
			if (err) throw err;

			for(i=0; i < result.length; i++) {
				eth_block_heights.push(result[i]['blocknumber'])
				eth_block_ts.push(result[i]['ts'])
				eth_block_tnums.push(result[i]['tnum'])
				eth_block_miners.push(result[i]['miner'])
				eth_block_sizes.push(result[i]['size'])
			}
			res.render('index', { eth_block_heights: eth_block_heights, eth_block_ts: eth_block_ts, eth_block_tnums: eth_block_tnums, eth_block_miners: eth_block_miners, eth_block_sizes: eth_block_sizes });
		});
		
  		var eth_txn_sql = "select hash as txnhash, '10 minutes ago' as age, ethValue as amt from ethtransaction order by hash desc limit 3";
		con.query(eth_txn_sql, function(err, result, fields) {
			if (err) throw err;
			for(i=0; i < result.length; i++) {
				eth_txn_txnhash.push(result[i]['txnhash'])
				eth_txn_age.push(result[i]['age'])
				eth_txn_amt.push(result[i]['amt'])
			}
			res.render('index', {eth_txn_txnhash: eth_txn_txnhash, eth_txn_age: eth_txn_age, eth_txn_amt: eth_txn_amt});
		})
		

	});
	*/
	
})

app.get('/eth/block', function(req, res) {
	console.log("Rendering block information page");
	res.render('block');
})

app.listen(3000, function () {
  console.log('Server running on port 3000!')
})