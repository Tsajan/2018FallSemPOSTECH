create table btcblock (
	hash char(64) not null primary key,
	confirmations integer unsigned,
	strippedsize integer unsigned,
	size integer unsigned,
	weight integer unsigned,
	height integer unsigned not null,
	version integer,
	versionHex char(8),
	merkleroot char(64) not null,
	tx json,
	ts timestamp not null,
	mediantime timestamp,
	nonce integer unsigned not null,
	bits char(8),
	difficulty double unsigned,
	chainwork char(64),
	ntx integer unsigned,
	previousblockhash char(64),
	nextblockhash char(64)
);

create table btctransaction (
	txid char(64) not null primary key,
	hash char(64) not null,
	version integer,
	size integer,
	vsize integer,
	weight integer,
	locktime integer,
	vin json,
	vout json
);

create table ethTransaction (
	hash char(66) not null primary key,
	nonce integer not null,
	blockHash varchar(66) not null,
	blockNumber integer unsigned not null,
	transactionIndex integer unsigned not null,
	fromAddr char(42) not null,
	toAddr char(42) not null,
	ethValue varchar(64) not null,
	gasPrice varchar(64) not null,
	gas integer unsigned not null,
	input varchar(514)
);

select blocknumber, ts, 12, miner, size from ethBlock order by blockNumber desc limit 3;

create table ethBlock (
	blocknumber integer unsigned not null,
	hash char(66) not null primary key,
	parentHash char(66) not null,
	nonce varchar(18) not null,
	sha3Uncles varchar(66),
	logsBloom varchar(514),
	transactionsRoot varchar(66),
	receiptsRoot varchar(66),
	mixHash varchar(66),
	stateRoot varchar(66),
	miner varchar(42) not null,
	difficulty varchar(64) not null,
	totalDifficulty varchar(64) not null,
	extraData varchar(128),
	size integer not null,
	gasLimit integer not null,
	gasUsed integer not null,
	ts timestamp not null,
	transactions JSON,
	uncles JSON
);

insert into ethTransaction values(
	'5a25631ad35b7a3a80b97f0cb980b7b1d576adf6208a377152ea18d6d2e6b891',
	91,
	'1c23c8e34ff051b6a532dbdd32e3fa3e89b98960834cd23ff03f255ca165852d',
	6725295,
	0,
	'b889560c9d4c186a87ef6a9ec6d3351ec2a17801',
	'9c999c5da009d3a208f0178b637a50f4ef42400f',
	'28.55450000',
	'0.000000008',
	72350,
	'a9059cbb0000000000000000000000001951e479aaa8b8d4f083faa354841efd2e5456a20000000000000000000000000000000000000000000000056bc75e2d63100000'
);

insert into ethBlock(
	blocknumber,
	hash,
	parentHash,
	nonce,
	sha3Uncles,
	logsBloom,
	transactionsRoot,
	stateRoot,
	miner,
	difficulty,
	totalDifficulty,
	extraData,
	size,
	gasLimit,
	gasUsed,
	ts) values
	(6751293, 
	'88a9ebe34ff051b6a532dbdd32e3fa3e89b98960834cd23ff03f255ca165852d',
	'6603a63fc7649e83c040ca28f4b9fb32a7a9cbdb9a37002e5ca73d0883585896',
	'1e42bad4154a9444',
	'1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347',
	'LogbloomsEmpty5',
	'TxnRoot5',
	'StateRoot5',
	'b2930b35844a230f00e51431acae96fe543a0347',
	'2,711,836,409,579,203',
	'7,946,770,078,200,902,000,000',
	'ExtraData5',
	'19088',
	8000029,
	7998547,
	'2018-11-22 08:52:11'
);