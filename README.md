# Naarad
An app for code.fun.do++ by **team R.A.Y.** <br>
Team Members: <br>
* [Yash Jipkate](https://github.com/YashJipkate)<br>
* [Rupal Sharma](https://github.com/rsdel2007)<br>
* [Anshul Shrivastava](https://github.com/anshulll)<br>

## The Problem
As soon as the disaster strikes there is a flood of email and messages to the concerned NGO or government helpline, this makes the task even harder for the NGO or the government agencies to sort the calls and figure out the exact picture of the problem and the figures of those in distress.

## Our Solution
We aim to build an Android app that filters these messages and delivers only useful and crucial information to the NGO or the agency. 
  * On the client side, the app would have a simple user-friendly interface with a button to record his message or type it. 
  * If the user inputs voice message, we shall convert it to text internally. 
  * We would then send the text message to the backend server which would apply NLP algorithms to classify the messages into categories like:-
    * Sympathy/Support/Donation
    * Call for Help/Missing/Casualities/Damage
    * Spam/Not Useful
    * Caution/advice/information
    
    ![alt WORKFLOW](https://github.com/rsdel2007/Naarad/blob/master/Images/Workflow.jpeg)
    
    
  * We will form a database which will carry the information of the agencies or the organization.<br>
  The filtered and categorized messages will now be sent to the concerned NGO/agency which works in the direction of the specified category. <br>
  For example, if a message is classified in the category of sympathy/support/donation then this message will be conveyed to the
  respected organization which may help the person as fast as possible.<br>
  * The messages send in the category _Caution/advice/information_ will be sent to the user in the app to all the users.
  * The location of the app user will be tracked and we use this information to help him during the disaster hit nearby his location. The user will be provided with information regarding where to evacuate and safest place nearby him. 
  * We will use facebook and twitter API for extracting the tweets related to the disaster using the hashtag and other things. After extracting all the tweets then we will use our classifier which will classify the tweets into the given categories. After that, the responsible agency or org will be conveyed about the issue.
 


