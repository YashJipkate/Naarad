from flask import Flask, request, jsonify
import os, uuid, sys
import sklearn
from flask_mail import Mail, Message

from gnewsclient import gnewsclient

gc = gnewsclient()





def g_news():
    news = []
    gc.topic = 'top stories'
    gc.location = "kerala"
    gc.query = "floods"
    india_editions = ['India (English)','India (Telugu)','India (Hindi)','India (Malayalam)','India (Tamil)']
    for editions in india_editions:
        gc.edition = editions
        n = gc.get_news()
        for index in range(len(n)):
            news.append(n)
    return news

#news = gnews()

app = Flask(__name__)

app.config.update(
    DEBUG=True,
	MAIL_SERVER='smtp.gmail.com',
	MAIL_PORT=465,
	MAIL_USE_SSL=True,
	MAIL_USERNAME = 'naarad.cfdpp@gmail.com',
	MAIL_PASSWORD = 'naaradCFD++'
)

mail = Mail(app)

@app.route('/gnews', methods = ['GET','POST'])

def gnews():
    if request.method == 'POST':
        return "<h1>Wrong Request<h1>"

    else:
        content = request.get_json()

        response = {}
        response['status'] = "OK"
        news = g_news()
        msg = Message("Mail from Naarad", 
                      sender="naarad.cfdpp@gmail.com",
                      recipients=["rupalsharmadelhi@gmail.com","sshrivastavanshul@gmail.com"])
        i=0
        j=0
        msg.body = ""
        for i in range(len(news)):
            for j in range(len(news[0])):
                msg.body += "Title\n" + news[i][j]["title"]+ "\nLink\n" + news[i][j]["link"] 
            mail.send(msg)
            print("Mail sent")


    return jsonify(response)


if __name__ == "__main__":
    app.run()
