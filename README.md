# Naarad
An app for code.fun.do++

## The Problem
As soon as the disaster strikes there is a flood of email and messages to the concerned NGO or government helpline. Many times the victims in distress send repeated messages in panic, this makes the task even harder for the NGO or the government agencies to sort the calls and figure out the exact picture of the problem and the figures of the distressed.

## Our Solution
We aim to build an Android app that filters these messages and delivers only useful and crucial information to the NGO or the agency. 
  * On the client side, the app would have a simple user-friendly interface with a button to record his message or type it. 
  * If the user inputs voice message, we shall convert it to text internally. 
  * We would then send the text message to the backend server which would apply NLP algorithms to classify the messages into     categories like:-
    * Sympathy/Support/Donation
    * Call for Help/Missing/Casualities/Damage
    * Spam
  * The filtered and categorized messages will now be sent to the concerned NGO/agency which works in the direction of the       specified category.
