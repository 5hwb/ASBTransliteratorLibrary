# ASB Transliterator App

## About

This is a GUI Java program that aims to transliterate traditional English orthography into more phonetic ways to write English, as well as transliterate between different phonetic orthographies and scripts.

At this point in time, it can transliterate between phonetic English orthography (Even Better English Orthography, abbreviated as 'EBEO') and a collection of foreign scripts including Korean hangul, Thai script, and Tibetan script.

## How it works

Transliteration between scripts is done in the Replacer class. Each foreign script is represented by a subclass of Replacer, defining exactly what symbols are to be replaced. Symbols are referred to as 'ngraphs' as they can be either 1, 2 or more characters in length.

The Replacer algorithm works like this:

```
function translateFromToScript():
	create 3ElementStack to hold each ngraph
	create StringBuilder to store the output

	for each character in the input string:
		for integer in range(c, 0):
			Read a sub-string ngraph starting with current char and ending in integer c
			Look up Replacer's HashMap to see if the ngraph exists
			if (ngraph is found):
				3ElementStack.push(ngraph)
				break
			else:
				c -= 1

		get the corresponding ngraph in the target script, depending on the context of the surrounding ngraphs
		StringBuilder.append(correspondingNgraph)

	return StringBuilder.toString()
```

## EBEO DICTIONARY NOTES

The regularisation of the dictionary is not complete yet!
TODO: translate all affixes and suffixes to their correct pronunciation

## Regex replacements

To fix issues:
`\n([^aeiouāàéèēúíòōáùūó]{1,3})STRING\n`
to
`\n\1STRIŊ\n`

This is a better way to replace suffixes (ensures 1-syllable words don't get replaced by accident):
`([aeiouāàéèēúíòōáùūó])([^aeiouāàéèēúíòōáùūó]{1,3})STRING\n`
to
`\1\2STRIŊ\n`

https://regex101.com/r/vHBwU0/1

## Note

Use buildDict.sh to generate the traditional to phonetic dictionary for use by the Transliterator.

# Replacer Rules File JSON syntax

Because the current design has limitations (e.g. only 1 alt letter form, not flexible, hardcoded), I have decided to re-engineer the design of the Replacer class so that rules for each different script are read from an external file.

## 'rules' object

The replacement rules are located within the `RULES` object, implemented as a list of PhonemeRule objects.

### Phoneme fields

* `l1` - list of Script 1 letters. The last char is the default form, used if all rules are not valid for the context
* `l1type` - type of all Script 1 letters
* `l1rule` - list of rules for converting Script 2 to Script 1
* `l2` - list of Script 2 letters. As in `l1`, the last char is the default form
* `l2type` - type of all Script 2 letters
* `l2rule` - list of rules for converting Script 1 to Script 2

### Rule syntax

* `V` - match any vowel types
* `C` - match any consonant types
* `N` - match any numeral types
* `P` - match any punctuation types
* `#` - match the start or end of a sentence
* `<doubleConso>` - match the 'doubleConso' type only
* `.` - match any letter
* `!T` - match any type that is not the type T
* ` | ` - OR, for indicating that more than 1 rule is to be checked ('rule1 OR rule2 OR rule3')
* `c=2` - counter rule: match if the counter for this phoneme's type equals 2
* `v=0` - phoneme variant selection rule: match if the letter phoneme's selected variant is the 1st one (0 in the index)

#### Rule structure

`(previous letter)_(current letter)_(next letter)`

### Rule examples

* `C_._V` - match if the previous letter is a _consonant_ and the next letter is a _vowel_
* `V_._.` - match if the previous letter is a _vowel_ (the next letter can be any type)
* `c=6` - match if the counter for this letter's type equals 6
* `v=1` - match if the letter phoneme's selected variant is the 2nd one (1 in the index)
* `!C_._V` - match if the previous letter is not a _consonant_ and the next letter is a _vowel_
* `!C_._!V` - match if the previous letter is not a _consonant_ and the next letter is not a _vowel_

## 'types' object

There are 4 broad classes of characters: vowel, consonant, numeral and punctuation. Each one can be subclassed further into different subtypes by adding a list, e.g. vowel -> semivowel or non-linear vowel.

Each type is indicated by a Type object, where 'name' is the name of the type and 'extratypes' is a list of subtypes of this type.

## 'counters' object

This is where the special 'counters' are defined. Counters are assigned for a specific type and will only increment when a particular pattern is matched.

### Counter fields

* `type` - the type that this counter counts
* `maxNum` - maximum number of the counter before it resets
* `incrRule` - list of pattern rules defining when to increment the counter

### 'Rule compiler'
Rule strings are parsed into a more direct JSON-like format, which improves performance.

e.g. `!C_._V | P_._!V | c=6` becomes

```
"rule": {
	{
		"subrules": [
			{
				"type": "consonant",
				"not": true
			},
			{
				"type": "anything",
				"not": false
			},
			{
				"type": "vowel",
				"not": false
			}
		]
	},
	{
		"subrules": [
			{
				"type": "punctuation",
				"not": false
			},
			{
				"type": "anything",
				"not": false
			},
			{
				"type": "vowel",
				"not": true
			}
		]
	},
	{
		"cVal": 6
	}
}
```

This site may come in handy: http://lisperator.net/pltut/parser/

## TODO  

* Complete rule files for the following:
	* Quikscript
	* Shavian
	* Thai
	* Myanmar
	* Kannada
	* BEO
	* Deseret
* Create window for modifying the replacer rules, which will allow the user to import and export the rules as a JSON file
