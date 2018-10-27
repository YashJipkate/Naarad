from flask import Flask, request, jsonify
import pickle
import os, uuid, sys
import sklearn
from flask_mail import Mail, Message
# from azure.storage.blob import BlockBlobService, PublicAccess

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

# block_blob_service = BlockBlobService(account_name='picklemodelnaarad', account_key='46Ly8PR6m0KLKxhmnLgsyE5EIoKRTP7qX6BVTQ8a17XDiadBRbhlHtJunoNA0RuRvrnq7GfizDoAzUKf2GwlMA==')

#filename = 'fimodel.pkl'

# generator = block_blob_service.list_blobs("picklecontainer")
# for blob in generator:
#      print("\t Blob name: " + blob.name)
#      block_blob_service.get_blob_to_path(blob.name, filename, os.path.join(os.getcwd(), filename))


#block_blob_service.get_blob_to_path(blob.name, filename, os.path.join(os.getcwd(), filename))

#GS_clf= pickle.load(open(filename, 'rb'))

@app.route('/')
def hello():
    return 'Hello, World!'

@app.route('/predict', methods = ['GET','POST'])
def predict():
    if request.method == 'GET':
        return "<h1>Wrong Request<h1>"
    else:
        content = request.get_json()
    
        #predicted_GS=GS_clf.predict([content['line']])
        response = {}
        response['status'] = "OK"

        #response['class'] = str(predicted_GS[0])
        if content["class"]=="1":
            msg = Message("Mail from Naarad", 
                        sender="naarad.cfdpp@gmail.com",
                        recipients=["yashjipkate@gmail.com"])  # Organisation for the class Sympathy/Support/Donation
            msg.body = "Help Wanted!\n" + content['line'] + "\nSent by Naarad user from " + content['address']            
            mail.send(msg)
            print("mail sent")                        
        elif content["class"]=="2":
            msg = Message("Mail from Naarad", 
                    sender="naarad.cfdpp@gmail.com",
                    recipients=["rupalsharmadelhi@gmail.com"])  # Organisation for the class Help/Missing/Casualities/Damage
            msg.body = "Help Wanted!\n" + content['line'] + "\nSent by Naarad user from " + content['address']            
            mail.send(msg)
            print("mail sent")
        elif content["class"]=="4":

            msg = Message("Mail from Naarad", 
                    sender="naarad.cfdpp@gmail.com",
                    recipients=["sshrivastavanshul@gmail.com"]) # Organisation for the class Caution/advice/information         
            msg.body = "Help Wanted!\n" + content['line'] + "\nSent by Naarad user from " + content['address']            
            mail.send(msg)
            print("mail sent")
                      


        return jsonify(response)


if __name__ == "__main__":
    app.run()
