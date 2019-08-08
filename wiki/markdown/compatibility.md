# Compatibility

|              |Front-end ➡|        |        |        |
|-------------:|:---------:|:------:|:------:|:------:|
|**Back-end ⬇**|       1.0.0|    1.0.1|    1.0.2|    1.0.3|
|         1.0.0|           ✔️️|        ✔️️|       ✅|       ✅|
|         1.0.1|           ✔️️|        ✔️️|       ✅|       ✅|
|         1.0.2|          ❕|       ✅|        ✔️|        ✔️|

✔️️ Yes | ✅ Yes, minor Issue | ❕ Yes, medium Issue | ❌ No, major Issue
<br>
<br>
## Back-end: 1.0.0
### Front-end: 1.0.0
Compatible: ✔️️ Yes

Notes: -

### Front-end: 1.0.1
Compatible: ✔️️ Yes

Notes:
- Query integration test have a miss match in the expected result: <br/>
expected:<{"[queryResponseEntity":{}]}> but was:<{"[responseEntity":"[]"]}>

### Front-end: 1.0.2
Compatible: ✅ Yes, minor Issue

Notes:
- Query integration test have a miss match in the expected result: <br/>
expected:<{"[queryResponseEntity":{}]}> but was:<{"[responseEntity":"[]"]}>
- Documentation is Agen based but it’s still functional since the jmespath reference to purl package instead of gav does not change subscription structure.

### Front-end: 1.0.3
Compatible: ✅ Yes, minor Issue

Notes:
- Query integration test have a miss match in the expected result: <br/>
expected:<{"[queryResponseEntity":{}]}> but was:<{"[responseEntity":"[]"]}>
- Documentation is Agen based but it’s still functional since the jmespath reference to purl package instead of gav does not change subscription structure.

## Back-end: 1.0.1
### Front-end: 1.0.0
Compatible: ✔️️ Yes

Notes:
- Subscription integration test have an error where notification meta is detected to be an invalid mail which is correct, but the test does not detect this and 2 tests will fail as a result.
- Query integration test have a miss match in the expected result: <br>
expected:<{"[responseEntity":"[]"]}> but was:<{"[queryResponseEntity":{}]}>

### Front-end: 1.0.1
Compatible: ✔️️ Yes

Notes:
- Query integration test have a miss match in the expected result: <br>
expected:<{"[queryResponseEntity":{}]}> but was:<{"[responseEntity":"[]"]}>

### Front-end: 1.0.2
Compatible: ✅ Yes, minor Issue

Notes:

- Documentation is Agen based but it’s still functional since the jmespath reference to purl package instead of gav does not change subscription structure.

### Front-end: 1.0.3
Compatible: ✅ Yes, minor Issue

Notes:

- Documentation is Agen based but it’s still functional since the jmespath reference to purl package instead of gav does not change subscription structure.

## Back-end: 1.0.2
### Front-end: 1.0.0
Compatible: ❕ Yes, medium Issue

Notes:
- Subscription integration test have an error where notification meta is detected to be an invalid mail which is correct, but the test does not detect this and 2 tests will fail as a result.
- Query integration test have a miss match in the expected result: <br>
expected:<{"[responseEntity":"[]"]}> but was:<{"[queryResponseEntity":{}]}>
- Documentation is Toulouse based but it’s still functional since the jmespath reference to gav package instead of purl does not change subscription structure.
- Multiple e-mail recipients are not supported through the front-end subscription form. 

### Front-end: 1.0.1
Compatible: ✅ Yes, minor Issue

Notes:

- Query integration test have a miss match in the expected result: <br>
expected:<{"[responseEntity":"[]"]}> but was:<{"[queryResponseEntity":{}]}>
- Documentation is Toulouse based but it’s still functional since the jmespath reference to gav package instead of purl does not change subscription structure.

### Front-end: 1.0.2
Compatible: ✔️️ Yes

Notes: -

### Front-end: 1.0.3
Compatible: ✔️️ Yes

Notes: -