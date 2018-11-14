const express = require('express');
const app = express();
const fs = require('fs');

var bodyParser = require('body-parser');
var Web3 = require('web3');
var w3 = new Web3();

var logger = fs.createWriteStream('txnHashList.txt', {
  flags: 'a' // 'a' means appending (old data will be preserved)
})

var txnList = [];

w3.setProvider(new Web3.providers.HttpProvider('http://localhost:8585'));

app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('public'));
app.set('view engine', 'ejs')

app.get('/', function (req, res) {
	var accounts = w3.personal.listAccounts;
	console.log(accounts)
	var balArray = [];
	for(var i=0; i < accounts.length; i++) {
		balArray.push(w3.fromWei(w3.eth.getBalance(accounts[i]),'ether'));
	}
	console.log(balArray.toString());
	var addressBalanceMap = {};
	accounts.forEach((key, i) => addressBalanceMap[key] = balArray[i].toString());
	console.log(addressBalanceMap);
	res.render('index', { accounts: accounts, balArray: balArray });
})

app.post('/', function (req, res) {
	var accounts = w3.personal.listAccounts;
	console.log(accounts)
	var balArray = [];
	for(var i=0; i < accounts.length; i++) {
		balArray.push(w3.fromWei(w3.eth.getBalance(accounts[i]),'ether'));
	}
	console.log(balArray.toString());
	var addressBalanceMap = {};
	accounts.forEach((key, i) => addressBalanceMap[key] = balArray[i].toString());
	console.log(addressBalanceMap);
	res.render('index', { accounts: accounts, balArray: balArray });  
})

function unlockAccountIfNeeded(account, password) {
	console.log("Account " + account + " is locked. Unlocking ...")
	w3.personal.unlockAccount(account, password, 300);
}

function sendAmt(sender, receiver, amt) {
	var txHash = w3.eth.sendTransaction({
		from: sender,
		to: receiver,
		value: w3.toWei(amt,'ether')
	});
	console.log("Current Transaction Hash: " + txHash);
	logger.write(txHash);
	logger.write('\n');
}

app.post('/processRequest', function(req, res) {
	//print the POST variables in console
	var obj = req.body.transmitter + " " + req.body.recipient + " " + req.body.sendAmount + " " + req.body.pwd;
	console.log(obj.toString());

	//store POST variable for send operation
	var sender = req.body.transmitter;
	var receiver = req.body.recipient;
	var amt = parseFloat(req.body.sendAmount);
	var pwd = req.body.pwd;

	unlockAccountIfNeeded(sender, pwd, function(err, res) {
		if(err) {
			console.log("Password Error! Try Again");
			res.send("Password Error! Try Again");
		}
	});
	sendAmt(sender, receiver, amt);
	res.send("Transaction Sent. Wait for block to be mined");
})

app.post('/viewTxn', function(req, res) {
	txnList = fs.readFileSync('txnHashList.txt').toString().split("\n");
	console.log(txnList);
	res.render('viewtxn', { txnList : txnList });
})

app.post('/viewTxnDetails', function(req, res) {
	var selectedTxHash = req.body.selectedTxn
	var txnDetails = w3.eth.getTransaction(selectedTxHash);
	res.send(txnDetails);
})

app.post('/searchBlock', function(req, res) {
	res.render('block')
})

app.get('/block', callBlk);
function callBlk(req, res){
   let number = req.query.blockNumber;
   w3.eth.getBlock(number, function(err, Blk){ 
      console.log('Block: '+ number + ' is sent ...');
      console.log(Blk);
      res.send(Blk);
   });
}

app.listen(3000, function () {
  console.log('Example app listening on port 3000!')
})