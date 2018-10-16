from gnewsclient import gnewsclient
import pickle
from bs4 import BeautifulSoup
from urllib.request import urlopen

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
from gensim.summarization import summarize
from urllib.request import Request, urlopen
gc = gnewsclient()

filename = 'finalmodel.sav'
GS_clf= pickle.load(open(filename, 'rb'))

def is_valid_word(word):
    # Check if word begins with an alphabet
    return (re.search(r'^[a-zA-Z][a-z0-9A-Z\._]*$', word) is not None)

def handle_emojis(text):
    # Smile -- :), : ), :-), (:, ( :, (-:, :')
    text = re.sub(r'(:\s?\)|:-\)|\(\s?:|\(-:|:\'\))', ' EMO_POS ', text)
    # Laugh -- :D, : D, :-D, xD, x-D, XD, X-D
    text = re.sub(r'(:\s?D|:-D|x-?D|X-?D)', ' EMO_POS ', text)
    # Love -- <3, :for i in _1:
    #print(i)
    text = re.sub(r'(<3|:\*)', ' EMO_POS ', text)
    # Wink -- ;-), ;), ;-D, ;D, (;,  (-;
    text = re.sub(r'(;-?\)|;-?D|\(-?;)', ' EMO_POS ', text)
    # Sad -- :-(, : (, :(, ):, )-:
    text = re.sub(r'(:\s?\(|:-\(|\)\s?:|\)-:)', ' EMO_NEG ', text)
    # Cry -- :,(, :'(, :"(
    text = re.sub(r'(:,\(|:\'\(|:"\()', ' EMO_NEG ', text)
    return text

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
def preprocess_text(text):
    processed_text = []
    # Convert to lower case
    text = text.lower()
    # Replaces URLs with the word URL
    text = re.sub(r'((www\.[\S]+)|(https?://[\S]+))', 'URL', text)
    # Replace @handle with the word USER_MENTION
    text = re.sub(r'@[\S]+', 'USER_MENTION', text)
    # Replaces #hashtag with hashtag
    text = re.sub(r'#(\S+)', r' \1 ', text)
    # Remove RT (retext)
    text = re.sub(r'\brt\b', '', text)
    # Replace 2+ dots with space
    text = re.sub(r'\.{2,}', ' ', text)
    # Strip space, " and ' from text
    text = text.strip(' "\'')
    # Replace emojis with either EMO_POS or EMO_NEG
    text = handle_emojis(text)
    # Replace multiple spaces with a single space
    text = re.sub(r'\s+', ' ', text)
    stop = stopwords.words('english')
    tokens = [word for sent in nltk.sent_tokenize(text) for word in nltk.word_tokenize(sent)]
    tokens = [token for token in tokens if token not in stop]
    tokens = [word for word in tokens if len(word) >= 2]
    text= ' '.join(tokens)
    #words = text.split()
    #return ' '.join(processed_text)
    return text


def gnews():
    news = []
    gc.topic = 'top stories'
    gc.location = input("Enter the location ")
    gc.query = input("please be specific with the query ")
    india_editions = ['India (English)','India (Telugu)','India (Hindi)','India (Malayalam)','India (Tamil)']
    for editions in india_editions:
        gc.edition = editions
        n = gc.get_news()
        for index in range(len(n)):
            news.append(n)
    return news
        
def predict(news):
    class1 = []
    class2 = []
    class4 = []
    for i in range(len(news)):
        for j in range(len(news[i])):
            x = news[i][j]['title']
            x = preprocess_text(x)
            pred=GS_clf.predict([x])
            if(pred[0]==1):
                class1.append(news[i][j]['link'])
            if(pred[0]==2):
                class2.append(news[i][j]['link'])
            if(pred[0]==4):
                class4.append(news[i][j]['link'])
    return set(class1),set(class2),set(class4)

def get_text(url):
    
    page = urlopen(url)
    soup = BeautifulSoup(page)
    text = ' '.join(map(lambda p: p.text, soup.find_all('p')))
 
    return text

def aget_text(url):
    req = Request(url)
    webpage = urlopen(req).read()
    webpage = str(webpage)
    webpage = preprocess_text(webpage)
    return webpage

def summarizer(text):
    return summarize(text)

def main():
    class1=[]
    class2=[]
    class4=[]
    news = gnews()
    l1,l2,l4 = predict(news)
    for url in l1:
        text = get_text(url)
        #text = aget_text(url)
        summary = summarizer(text)
        class1.append(summary)
    for url in l2:
        text = get_text(url)
        summary = summarizer(text)
        class2.append(summary)
    for url in l4:
        text = get_text(url)
        summary = summarizer(text)
        class4.append(summary)
    return class1,class2,class4

key = input("Type rupal")
if start=="rupal"
class1,class2,class4 = main()
