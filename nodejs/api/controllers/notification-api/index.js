'use strict';

const HELPER_BASE = process.env.HELPER_BASE || "/opt/";
const Response = require(HELPER_BASE + 'response');

const crypto = require('crypto');
const sqlite3 = require("sqlite3");
const admin = require("firebase-admin");

const NOTIFICATION_TABLE_NAME = "notification";
const API_KEY = "12345678";
const NOTIFICATION_FILE_PATH = process.env.THIS_BASE_PATH + '/data/notification/notification.db';

const FIREBASE_CREDENTIAL_FILE = "./keys/【Firebaseのクレデンシャルファイル】";

const db = new sqlite3.Database(NOTIFICATION_FILE_PATH);
db.each("SELECT COUNT(*) FROM sqlite_master WHERE TYPE = 'table' AND name = '" + NOTIFICATION_TABLE_NAME + "'", (err, row) =>{
  if( err ){
    console.error(err);
    return;
  }
  if( row["COUNT(*)"] == 0 ){
    db.run("CREATE TABLE '" + NOTIFICATION_TABLE_NAME + "' (id TEXT PRIMARY KEY, messageTopic TEXT, client_id TEXT, messageTitle TEXT, messageText TEXT, datetime INTEGER)", (err, row) =>{
      if( err ){
        console.error(err);
        return;
      }
    });
  }
});

const serviceAccount = require(FIREBASE_CREDENTIAL_FILE);
admin.initializeApp({
	credential: admin.credential.cert(serviceAccount)
});

exports.handler = async (event, context, callback) => {
	var body = JSON.parse(event.body);
	console.log(body);

	if( event.requestContext.apikeyAuth.apikey != API_KEY )
		throw "apikey invalid";
	
	if( event.path == '/notification-push-message' ){
		var topic = body.topic;
		var title = body.title;
		var client_id = body.client_id;
		var datetime = body.datetime;
		var body = body.body;

		if( !datetime )
			datetime = new Date().getTime();
		var id = crypto.randomUUID();

		await new Promise((resolve, reject) =>{
			db.run("INSERT INTO '" + NOTIFICATION_TABLE_NAME + "' (id, messageTopic, client_id, messageTitle, messageText, datetime) VALUES (?, ?, ?, ?, ?, ?)", [id, topic, client_id, title, body, datetime], (err) =>{
				if( err )
					return reject(err);
				resolve({});
			});
		});

		var msg = {
			notification: {
				title: client_id + ":" + title,
				body: body
			},
			data: {
				title: client_id + ":" + title,
				body: body,
				id: id,
				datetime: String(datetime)
			},
			topic: topic
		};
		var result = await admin.messaging().send(msg);
		return new Response({ result: result });
	}else
	
	if( event.path == '/notification-get-list' ){
		var topic = body.topic;
		var start = body.start;
		var end = body.end;
    return new Promise((resolve, reject) =>{
      db.all("SELECT * FROM '" + NOTIFICATION_TABLE_NAME + "' WHERE messageTopic = ? AND datetime BETWEEN ? AND ? ORDER BY datetime DESC", [topic, start, end], (err, rows) => {
				if( err )
          return reject(err);
        resolve(new Response({ rows: rows }));
      });
    });
	}else

	if( event.path == '/notification-delete-allmessage' ){
		var topic = body.topic;
    return new Promise((resolve, reject) =>{
      db.all("DELETE FROM '" + NOTIFICATION_TABLE_NAME + "' WHERE messageTopic = ?", [topic], (err) => {
				if( err )
          return reject(err);
        resolve(new Response({}));
      });
    });
	}else

	{
		throw "unknown endpoint";
	}
};
