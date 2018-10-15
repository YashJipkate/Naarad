import pickle
import tweepy


consumer_key= 'DE2E8xPdHjk6iMmx8r7u4gohq'
consumer_secret= 'AoK1pw28ShUq9ccgnjlO2qErwRFJ5YvXHX33ZZuww4rrGXsr2v'
access_token= '1046384767724400640-KeCnqiAdUnaqPrjzq1CHhwyKSdQHtU'
access_token_secret = 'vjlPsqsc2mjfNqlL5e0lLrLDVcHdzylzSvY0hLYXRiGJ1'

auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
auth.set_access_token(access_token, access_token_secret)
api = tweepy.API(auth)


filename = 'finalmodel.sav'
GS_clf= pickle.load(open(filename, 'rb'))


from nltk.stem.porter import PorterStemmer
from nltk.stem.porter import *
stemmer=PorterStemmer()
import random
import numpy as np
from nltk.corpus import stopwords
import pandas as pd
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
import nltk

def is_valid_word(word):
    # Check if word begins with an alphabet
    return (re.search(r'^[a-zA-Z][a-z0-9A-Z\._]*$', word) is not None)

def handle_emojis(tweet):
    # Smile -- :), : ), :-), (:, ( :, (-:, :')
    tweet = re.sub(r'(:\s?\)|:-\)|\(\s?:|\(-:|:\'\))', ' EMO_POS ', tweet)
    # Laugh -- :D, : D, :-D, xD, x-D, XD, X-D
    tweet = re.sub(r'(:\s?D|:-D|x-?D|X-?D)', ' EMO_POS ', tweet)
    # Love -- <3, :*
    tweet = re.sub(r'(<3|:\*)', ' EMO_POS ', tweet)
    # Wink -- ;-), ;), ;-D, ;D, (;,  (-;
    tweet = re.sub(r'(;-?\)|;-?D|\(-?;)', ' EMO_POS ', tweet)
    # Sad -- :-(, : (, :(, ):, )-:
    tweet = re.sub(r'(:\s?\(|:-\(|\)\s?:|\)-:)', ' EMO_NEG ', tweet)
    # Cry -- :,(, :'(, :"(
    tweet = re.sub(r'(:,\(|:\'\(|:"\()', ' EMO_NEG ', tweet)
    return tweet

def preprocess_word(word):
    # Remove punctuation
    word = word.strip('\'"?!,.():;')
    # Convert more than 2 letter repetitions to 2 letter
    # funnnnny --> funny
    word = re.sub(r'(.)\1+', r'\1\1', word)
    # Remove - & '
    word = re.sub(r'(-|\')', '', word)
    return word

stop_words = set(stopwords.words('english'))
def preprocess_tweet(tweet):
    processed_tweet = []
    # Convert to lower case
    tweet = tweet.lower()
    # Replaces URLs with the word URL
    tweet = re.sub(r'((www\.[\S]+)|(https?://[\S]+))', 'URL', tweet)
    # Replace @handle with the word USER_MENTION
    tweet = re.sub(r'@[\S]+', 'USER_MENTION', tweet)
    # Replaces #hashtag with hashtag
    tweet = re.sub(r'#(\S+)', r' \1 ', tweet)
    # Remove RT (retweet)
    tweet = re.sub(r'\brt\b', '', tweet)
    # Replace 2+ dots with space
    tweet = re.sub(r'\.{2,}', ' ', tweet)
    # Strip space, " and ' from tweet
    tweet = tweet.strip(' "\'')
    # Replace emojis with either EMO_POS or EMO_NEG
    tweet = handle_emojis(tweet)
    # Replace multiple spaces with a single space
    tweet = re.sub(r'\s+', ' ', tweet)
    stop = stopwords.words('english')
    tokens = [word for sent in nltk.sent_tokenize(tweet) for word in nltk.word_tokenize(sent)]
    tokens = [token for token in tokens if token not in stop]
    tokens = [word for word in tokens if len(word) >= 2]
    tweet= ' '.join(tokens)
    #words = tweet.split()
    #return ' '.join(processed_tweet)
    return tweet

def predict(x):
    x=preprocess_tweet(x)
    predicted_GS=GS_clf.predict([x])
    return predicted_GS[0]

query = input("Please be specific with the hashtag")
number = "100"
loop = int(input("Enter a number"))
for i in range(loop):
    results = api.search(lang="en",q=query + " -rt",count=number,result_type="recent")
    for c, result in enumerate(results, start=1):
        tweet = result.text
        tidy_tweet = tweet.strip().encode('ascii', 'ignore')
        tidy_tweet = str(tidy_tweet)
        pred = predict(tidy_tweet)
        print(c," ",tidy_tweet," ",pred)        
