# ASB Transliterator App

## About

This is a GUI Java program that aims to transliterate between different phonetic orthographies and scripts.

At this point in time, it can transliterate between phonetic English orthography (Even Better English Orthography, abbreviated as 'EBEO') and a collection of foreign scripts including Korean hangul and Khmer script.

## How it works

Transliteration between scripts is done in the ExternalFileReplacer class. Each writing system is represented with a 'replacer rules file', an external JSON file containing a list of rules that map the correct graphemes from EBEO to the target script.

The Replacer algorithm works like this:

```
function translateFromToScript():
	create 3ElementStack to hold each grapheme
	create StringBuilder to store the output

	for each character in the input string:
		for integer in range(c, 0):
			Read a sub-string grapheme starting with current char and ending in integer c
			Look up Replacer's HashMap to see if the grapheme exists
			if (grapheme is found):
				3ElementStack.push(grapheme)
				break
			else:
				c -= 1

		get the corresponding grapheme in the target script, depending on the context of the surrounding graphemes
		StringBuilder.append(correspondingGrapheme)

	return StringBuilder.toString()
```

## Performance

It currently takes ~90ms to transliterate a 62.7KB text file.

## Refactoring idea

The code, as it is now, is very complex and hard to maintain. `translateFromToScript()` and `selectRule()` are both very long methods that can be split up.

Ideas:

* In `translateFromToScript()`, move the 'grapheme insertion to output' to their own functions
* Make rule selection in `selectRule()` based on new classes: RuleParser, with subclasses PatternRuleParser, CounterRuleParser and PhonemeVariantRuleParser

# Replacer Rules File JSON syntax

## Rule syntax

* ` | ` - OR, for indicating that any one of the sub-rules can be a match for the whole rule to match ('rule1 OR rule2 OR rule3...')
* ` & ` - AND, for indicating that all of the sub-rules must be a match for the whole rule to match ('rule1 AND rule2 AND rule3...')

There are 3 types of rules:

1. **Pattern matching rule**: Match if and only if the types of this character and both surrounding characters match the given pattern. A pattern consists of 3 tokens separated by an underscore, with each token representing the type(s) to be matched. Rule syntax is as following: `(previous letter token)_(current letter token)_(next letter token)`
2. **Counter rule**: Match if and only if the counter for this phoneme's type equals the given number. Comes in the following form: `c=n` where `n` is a positive integer.
3. **Phoneme variant selection rule**: Match if and only if the particular variant of the selected grapheme equals the given number. Comes in the following form: `pv=n` where `n` is a positive integer.

### Pattern matching rule token syntax

* `V` - match any vowel types
* `C` - match any consonant types
* `N` - match any numeral types
* `P` - match any punctuation types
* `#` - match the start or end of a sentence
* `<doubleConso>` - match the 'doubleConso' type only
* `.` - match any letter
* `!T` - match any type that is not the type T

### Rule examples

* `C_._V` - match if the previous letter is a _consonant_ and the next letter is a _vowel_
* `V_._.` - match if the previous letter is a _vowel_ (the next letter can be any type)
* `c=6` - match if the counter for this letter's type equals 6
* `pv=1` - match if the letter phoneme's selected variant is the 2nd one (1 in the index)
* `!C_._V` - match if the previous letter is not a _consonant_ and the next letter is a _vowel_
* `!C_._!V` - match if the previous letter is not a _consonant_ and the next letter is not a _vowel_
* `<narrowConso>_._.` - match if the previous letter is of the 'narrowConso' type
* `V_._. | !C_._V` - match any one of the 2 given rules: the previous letter is a _vowel_, **or** the previous letter is not a _consonant_ and the next letter is a _vowel_

### Pattern selection and counter rule example

```
{
	"l1": ["M", "m"],	
	"l1type": "consonant",
	"l1rule": ["#_._."],
	
	"l2": ["ម", "្ម"],	
	"l2type": "consonant",
	"l2rule": ["<narrowConso>_._. | !C_._. | c=2"]
},
```

What this does:

* Map any L1 letters to 'ម' if it matches the first L2 rule: previous consonant was of type 'narrowConso', previous consonant is not a consonant type, or the counter for consonant types has reached 2; or '្ម' otherwise.
	* This ensures that the subscript variant of ម is chosen in the right context
* Map any L2 letters to 'M' if the previously selected grapheme was a sentence boundary (represented with #); or 'm' otherwise.
	 * This ensures that sentences always start with a capital letter

### Phoneme variant selection rule example

```
{
	"l1": ["1a", "2a", "3a"],
	"l1type": "vowel", 
	"l1rule": ["pv=2", "pv=1", "pv=0"],
	
	"l2": ["oneA", "twoA", "triA"], 
	"l2type": "vowel", 
	"l2rule": ["pv=0", "pv=1", "pv=2"]
}
```

To script:

* Input: `1a 2a 3a`
* Output: `oneA twoA triA`

From script:

* Input: `oneA twoA triA`
* Output: `3a 2a 1a`

## 'rules' object

The replacement rules are located within the `RULES` object, implemented as a list of PhonemeRule objects.

### Phoneme fields

* `l1` - list of Script 1 letters. The last char is the default form, used if all rules are not valid for the context
* `l1type` - type of all Script 1 letters
* `l1rule` - list of rules for converting Script 2 to Script 1
* `l2` - list of Script 2 letters. As in `l1`, the last char is the default form
* `l2type` - type of all Script 2 letters
* `l2rule` - list of rules for converting Script 1 to Script 2

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
	* Deseret
* Create window for modifying the replacer rules, which will allow the user to import and export the rules as a JSON file
