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
	var btcBlockJSON;
	var btcTxnJSON;

	console.log("Starting Blockchain Monitoring System");

	connection.beginTransaction(function (err) {
        var eth_txn_sql = "select hash as txnhash, '10 minutes ago' as age, ethValue from ethtransaction order by hash desc limit 5";
        var eth_block_sql = "select blocknumber, date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, 12 as tnum, miner, size from ethBlock order by blockNumber desc limit 5";

        var btc_block_sql = `select 
    							bb.height as height,
    							date_format(bb.ts, '%Y-%m-%d %H:%i:%s') as tstamp,
    							bb.ntx as txnnum,
    							json_extract(bt.vout,"$[0].scriptPubKey.addresses[0]") as miner,
    							bb.size as size
							from btctransaction bt
							inner join btcblock bb
							on bb.tx->"$[0]" = bt.txid
							order by height desc limit 5;`

		var btc_txn_sql = `select 
    							hash as txnhash,
    							sum_json_array(json_extract(vout, "$[*].value")) as txnamt,
    							size as size
							from btctransaction
							order by txnhash desc limit 5;`

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

        connection.query(btc_block_sql, function (err, rows, fields) {
            if (err) {
                console.log("Error while getting bitcoin block data");
            } else {
                console.log("Getting btcblock data");
                btcBlockJSON = rows;
            }
        });

        connection.query(btc_txn_sql, function (err, rows, fields) {
            if (err) {
                console.log("Error while getting bitcoin txn data");
            } else {
                console.log("Getting btctxn data");
                btcTxnJSON = rows;
            }
        });

        connection.commit(function (err, info) {
            console.log("commit");
        	res.render('index', { ethTransaction: ethTransJSON, ethBlock: ethBlockJSON, btcBlock: btcBlockJSON, btcTxn: btcTxnJSON });
        });

    });
})

app.post('/search', function(req, res) {
	
	var ethblockinfo;
	var btcblockinfo;
	var ethtxninfo;
	var btctxninfo;
	var ethblockinfobyheight;
	var btcblockinfobyheight;

	//arrays to be used while rendering data in ejs
	var btcTxnSenderAddrArr = [];
	var btcTxnSenderAmtArr = [];
	var btcTxnOutputValArr = [];
	var btcTxnOutputAddrArr = [];
	var inputSum = 0.0;

	console.log("Search started");
	var ethhashflg = 0;
	var btchashflg = 0;
	var inputField = req.body.searchval;
	console.log("User search query: " + inputField);
	if(inputField.startsWith('0x') && inputField.length == 66) {
		console.log("User probably searched for a ethereum hash value");
		ethhashflg = 1;
	}
  	
  	else if(inputField.length == 64) {
  		console.log("User probably searched for bitcoin hash value");
  		btchashflg = 1;
  	}

  	if(ethhashflg) {
  		var sql = `select count(*) as cnt from ethblock where hash = ? 
  					union all
  					select count(*) from ethtransaction where hash = ?;`;
  		connection.query(sql, [inputField, inputField], function(err, result, fields) {
  			if(err) throw err;
  			console.log(result);
  			//if there is a match from ethereum block table, fetch data from ethereum block table
  			if(result[0].cnt) {
  				console.log("Matching Record Found in ethBlock table");
  				var nextsql = `select 
  									blocknumber,
  									miner, 
  									date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, 
  									difficulty, 
	  								size, 
  									nonce, 
  									sha3uncles,  
  									ifnull(transactions,0) as numtxn, 
  									ifnull(uncles,0) as numuncles, 
  									totaldifficulty, 
  									gasused, 
  									gaslimit, 
  									hash, 
  									parenthash 
  								from ethblock where hash = ? limit 1;`;
  				connection.query(nextsql, [inputField], function(err, out, fields) {
  					if(err) throw err;
  					ethblockinfo = out;
  					console.log(out);
  					res.render('ethblock', { ethblockinfo: ethblockinfo });
  				});
  			}
  			//else if there is a match from ethereum transaction table, fetch the data from ethereum transaction table
  			else if(result[1].cnt) {
  				console.log("Matching Record Found in ethTransaction table");
  				var nextsql = `select
    								hash,
    								fromAddr,
    								toAddr,
    								ethValue,
    								blockNumber,
    								gasPrice,
    								gas,
    								nonce,
    								input
								from ethtransaction
								where hash = ? limit 1;`;
				connection.query(nextsql, [inputField], function(err, out, fields) {
					if(err) throw err;
					ethtxninfo = out;
					console.log(out);
					res.render('ethtxn', { ethtxninfo : ethtxninfo });
				});
  			}
  		});
  	}

  	if(btchashflg) {
  		var sql = `select count(*) as cnt from btcblock where hash = ?
					union all
					select count(*) from btctransaction where txid = ?;`;
		connection.query(sql, [inputField, inputField], function(err, result, fields) {
			if(err) throw err;
			console.log(result);
			//if there is a match from bitcoin block table, fetch data from bitcoin block table
			if(result[0].cnt) {
				console.log("Matching Record Found in btcBlock table");
				var nextsql = `select
    								hash,
    								height,
    								ntx,
								    date_format(ts,'%Y-%m-%d %H:%i:%s') as ts,
    								difficulty,
    								bits,
    								size,
    								weight,
    								version,
    								nonce,
    								previousblockhash,
    								nextblockhash,
    								merkleroot
								from btcblock where hash = ? limit 1;`
				connection.query(nextsql, [inputField], function(err, out, fields) {
					if(err) throw err;
					btcblockinfo = out;
					console.log(out);
					res.render('btcblock', { btcblockinfo: btcblockinfo });

				})
			}
			// else if there is a match from bitcoin transaction table, fetch data from bitcoin txn table
			else if(result[1].cnt) {
				connection.beginTransaction( function(err) {

					console.log("Matching Record found in btcTransaction table");
					var nextsql = `select
    								hash as txnhash,
    								json_extract(vin, "$[*].txid") as fromTxIDs,
    								json_extract(vin, "$[*].vout") as fromTxPos,
    								json_extract(vout, "$[*].scriptPubKey.addresses[0]") as toaddresses,
    								json_extract(vout, "$[*].value") as toval,
    								sum_json_array(json_extract(vout, "$[*].value")) as totalout,
    								size,
    								vsize,
    								locktime,
    								weight,
    								json_extract(vout, "$[*].scriptPubKey.asm") as outputscriptsig,
    								json_extract(vin, "$[*].scriptSig") as inputscriptsig
							from btctransaction where txid = ? limit 1;`;
					connection.query(nextsql, [inputField], function(err, out, fields) {
						if(err) throw err;
						btctxninfo = out;
					
						//get the string for sender transaction ids and transaction output positions
						btctxninputs = out[0].fromTxIDs;
						btctxnpos = out[0].fromTxPos;

						//remove double quotes and brackets from btctxninputs and btctxnpos
						btctxninputs = btctxninputs.replace(/"/g, "").replace("[", "").replace("]", "");
						btctxnpos = btctxnpos.replace(/"/g, "").replace("[", "").replace("]", "");

						btctxninputsArr = btctxninputs.split(", ");
						btctxnposArr = btctxnpos.split(", ");
					
						console.log("Transaction Details");
						console.log(btctxninfo);

						for(var i=0; i < btctxninputsArr.length; i++) {
							var anothersql = `select 
    										json_extract(vout, "$[?].scriptPubKey.addresses[0]") as sender,
    										json_extract(vout, "$[?].value") as senderval
										from btcTransaction where txid=?`;
							connection.query(anothersql, [parseInt(btctxnposArr[i]), parseInt(btctxnposArr[i]), btctxninputsArr[i]], function(err, out, fields) {
								if(err) throw err;
								btcTxnSenderAddrArr.push(out[0].sender);
								btcTxnSenderAmtArr.push(out[0].senderval);
							});
						}

						//get the string of to addresses and to values
						btctxnoutputaddr = out[0].toaddresses;
						btctxnoutputaddr = btctxnoutputaddr.replace(/"/g,"").replace("[","").replace("]","");
						btcTxnOutputAddrArr = btctxnoutputaddr.split(", ");

						btctxnoutputval = out[0].toval;
						btctxnoutputval = btctxnoutputval.replace(/"/g,"").replace("[","").replace("]","");
						btcTxnOutputValArr = btctxnoutputval.split(", ");

						connection.commit(function(err, info) {

							//remove double quotes from sender addresses
							for(var i=0; i < btcTxnSenderAddrArr.length; i++) {
								btcTxnSenderAddrArr[i] = btcTxnSenderAddrArr[i].replace(/"/g,"");
								inputSum += parseFloat(btcTxnSenderAmtArr[i]);
							}

							console.log(btcTxnSenderAmtArr);
							console.log(btcTxnSenderAddrArr);

							res.render('btctxn', { btctxninfo: btctxninfo, btcTxnSenderAddrArr: btcTxnSenderAddrArr, btcTxnSenderAmtArr: btcTxnSenderAmtArr, btcTxnOutputAddrArr: btcTxnOutputAddrArr, btcTxnOutputValArr: btcTxnOutputValArr, inputSum: inputSum });
						})


					});
				});
			}
		})
  	}

  	
  	//this is the search based on block numbers for both ethereum and bitcoin
  	if(!ethhashflg && !btchashflg) {
 		var sql = `select count(*) as cnt from ethblock where blocknumber = ?
 						union all
 						select count(*) from btcblock where height = ?`;
  		connection.query(sql, [inputField, inputField], function(err, result, fields) {
  			if(err) throw err;
  			console.log(result);

  			//if it finds a match from ethereum block table but not bitcoin table, fetch data from ethereum block table
  			if(result[0].cnt && !result[1].cnt) {
  				console.log("Matching record found in ethBlock table");
  				var nextsql = `select 
  									blocknumber,
  									miner, 
  									date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, 
  									difficulty, 
  									size, 
  									nonce, 
  									sha3uncles,  
  									ifnull(transactions,0) as numtxn, 
  									ifnull(uncles,0) as numuncles, 
  									totaldifficulty, 
  									gasused, 
  									gaslimit, 
  									hash, 
  									parenthash 
  								from ethblock where blocknumber = ? limit 1`;
  				connection.query(nextsql, [inputField], function(err, out, fields) {
  					if(err) throw err;
  					ethblockinfobyheight = out;
  					console.log(out);
  					res.render('ethblock', { ethblockinfo: ethblockinfobyheight });
  				})
  			}
  			//else if it finds a match from bitcoin block table but not etheruem table, fetch data from bitcoin block table
  			else if(result[1].cnt && !result[0].cnt) {
  				console.log("Matching record found in btcBlock table");
  				var nextsql = `select
    								hash,
    								height,
    								ntx,
								    date_format(ts,'%Y-%m-%d %H:%i:%s') as ts,
    								difficulty,
    								bits,
    								size,
    								weight,
    								version,
    								nonce,
    								previousblockhash,
    								nextblockhash,
    								merkleroot
								from btcblock where height = ? limit 1;` 
				connection.query(nextsql, [inputField], function(err, out, fields) {
					if(err) throw err;
					btcblockinfobyheight = out;
					console.log(out);
					res.render('btcblock', { btcblockinfo: btcblockinfobyheight });
				});
  			}
  			//else if there is a match in both bitcoin block table and ethereum block table
  			else if(result[0].cnt && result[1].cnt) {
  				console.log("Matching Record found in both btcBlock and ethBlock table");
  				res.render('tworecords', {inputField: inputField});
  			}
  			else {
  				console.log("No records found");
  				res.render('norecords', {inputField: inputField});
  			}
  		});
  	}
});


app.get('/eth/block', function(req, res) {
	var ethblocksearchbynum;

	let number = req.query.number;
	var ethblocksqlbynum = `select 
  						blocknumber,
  						miner, 
  						date_format(ts,'%Y-%m-%d %H:%i:%s') as ts, 
  						difficulty, 
  						size, 
  						nonce, 
  						sha3uncles,  
  						ifnull(transactions,0) as numtxn, 
  						ifnull(uncles,0) as numuncles, 
  						totaldifficulty, 
  						gasused, 
  						gaslimit, 
  						hash, 
  						parenthash 
  					from ethblock where blocknumber = ? limit 1`;
  	connection.query(ethblocksqlbynum, [number], function(err, out, fields) {
  		if(err) throw err;
  		ethblocksearchbynum = out;
  		console.log(ethblocksearchbynum);
  		res.render('ethblock', { ethblockinfo: ethblocksearchbynum});
  	});
});

app.get('/btc/block', function(req, res) {
	var btcblocksearchbynum;

	let number = req.query.number;
	var btcblocksqlbynum = `select
    					hash,
    					height,
    					ntx,
						date_format(ts,'%Y-%m-%d %H:%i:%s') as ts,
    					difficulty,
    					bits,
    					size,
    					weight,
    					version,
    					nonce,
    					previousblockhash,
    					nextblockhash,
    					merkleroot
					from btcblock where height = ? limit 1;`;
	connection.query(btcblocksqlbynum, [number], function(err, out, fields) {
		if(err) throw err;
		btcblocksearchbynum = out;
		console.log(btcblocksearchbynum);
		res.render('btcblock', { btcblockinfo: btcblocksearchbynum});
	});
})



app.listen(3000, function () {
  console.log('Server running on port 3000!')
})