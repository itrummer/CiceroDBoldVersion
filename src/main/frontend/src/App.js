import React, { Component } from 'react';
import './App.css';
import { Label, Button, PageHeader, Panel, FormGroup, ControlLabel, FormControl, HelpBlock, InputGroup, Grid, Row, Col } from 'react-bootstrap';
import $ from 'jquery';

class App extends Component {
  constructor() {
    super();
    this.state = {response: null};
    this.onSuccess = this.onSuccess.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
  }

  onSuccess(data) {
    // alert(JSON.stringify(data));
    this.setState({response: data});
  }

  onSubmit(data) {
    //alert(JSON.stringify(data));
    $.ajax({
      method: 'post',
      url:'http://127.0.0.1:8080/test',
      data: JSON.stringify(data),
      contentType: 'application/json',
      success: this.onSuccess,
      error: function(err){
          alert("err");
      }
    });
  }

  render() {
    return (
      <div className="App">
        <PageHeader>CiceroDB<small>: Optimizing Voice Output of Relational Data</small></PageHeader>
        <TestInstance onSubmit={this.onSubmit}></TestInstance>
        <TestResult value={this.state.response}></TestResult>
      </div>
    );
  }
}

function FieldGroup({ id, label, help, ...props }) {
  return (
    <FormGroup controlId={id}>
      <ControlLabel>{label}</ControlLabel>
      <FormControl {...props} />
      {help && <HelpBlock>{help}</HelpBlock>}
    </FormGroup>
  );
}

function NumberGroup({ id, label, help, suffix, ...props }) {
  return (
    <FormGroup controlId={id}>
      <ControlLabel>{label}</ControlLabel>
      <InputGroup>
        <FormControl {...props} />
        <InputGroup.Addon>{suffix}</InputGroup.Addon>
      </InputGroup>
      {help && <HelpBlock>{help}</HelpBlock>}
    </FormGroup>
  );
}

function SelectGroup({ id, label, help, options, ...props }) {
  return (
    <FormGroup controlId={id}>
      <ControlLabel>{label}</ControlLabel>
      <FormControl componentClass="select" {...props}>{
        options.map((value) => {
          return (
            <option key={value} value={value}>{value}</option>
          );
        })
      }
      </FormControl>
      {help && <HelpBlock>{help}</HelpBlock>}
    </FormGroup>
  );
}

class TestInstance extends Component {
  constructor(props) {
    super(props);
    this.state = {
      csvHeader: "name:String,rate:Number",
      csvBody: "Toloache,4.2\nBrooklyn Diner,3.8\n",
      maxAllowableContextSize: 2,
      maxAllowableNumericalDomainWidth: 2,
      maxAllowableCategoricalDomainSize: 2,
      timeout: 120,
      epsilon: 0.1
    };

    this.onSubmit = props.onSubmit;
    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  parseValue(target) {
    if (target.type === 'checkbox') {
      return target.checked;
    } else if (target.type === 'number') {
      return parseFloat(target.value);
    } else {
      return target.value;
    }
  }

  handleChange(event) {
    var target = event.target;
    var value = this.parseValue(target);
    var name = target.name;

    this.setState({
      [name]: value
    });
  }

  handleSubmit(event) {
    var data = {
      csvHeader: this.state.csvHeader,
      csvBody: this.state.csvBody,
      algorithm: this.state.algorithm,
      config: {
        maxAllowableContextSize: this.state.maxAllowableContextSize,
        maxAllowableNumericalDomainWidth: this.state.maxAllowableNumericalDomainWidth,
        maxAllowableCategoricalDomainSize: this.state.maxAllowableCategoricalDomainSize,
        timeout: this.state.timeout,
        epsilon: this.state.epsilon
      }
    };
    this.onSubmit(data);
  }

  render() {
    return (
      <Panel header="Test Instance">
        <form>
          <Grid>
            <Row className="show-grid">
              <Col md={4}>
              <SelectGroup id="formPresetCsv" label="PresetCsv" options={["restaurants","football","phones"]}/>
              </Col>
              <Col md={8}>
              <FieldGroup id="formCsvHeader" type="text" label="CsvHeader" value={this.state.csvHeader} name="csvHeader" onChange={this.handleChange}/>
              <FieldGroup id="formCsvBody" label="CsvBody" componentClass="textarea" rows="5" value={this.state.csvBody} name="csvBody" onChange={this.handleChange}/>
              </Col>
            </Row>
            <Row className="show-grid">
              <Col md={4}>
              <FieldGroup id="formMaxAllowableContextSize" label="Max Allowable Context Size" help="mS" type="number" value={this.state.maxAllowableContextSize} name="maxAllowableContextSize" onChange={this.handleChange}/>
              </Col>
              <Col md={4}>
              <FieldGroup id="formMaxAllowableNumericalDomainWidth" label="Max Allowable Numerical Domain Width" help="mW" type="number" value={this.state.maxAllowableNumericalDomainWidth} name="maxAllowableNumericalDomainWidth" onChange={this.handleChange}/>
              </Col>
              <Col md={4}>
              <FieldGroup id="formMaxAllowableCategoricalDomainSize" label="Max Allowable Categorical Domain Size" help="mC" type="number" value={this.state.maxAllowableCategoricalDomainSize} name="maxAllowableCategoricalDomainSize" onChange={this.handleChange}/>
              </Col>
            </Row>
            <Row className="show-grid">
              <Col md={4}>
              <NumberGroup id="formTimeout" label="Timeout" suffix="Sec" help="Timeout before defaulting to a naive result" type="number" value={this.state.timeout} name="timeout" onChange={this.handleChange}/>
              </Col>
              <Col md={4}>
              <FieldGroup id="formEpsilon" label="Epsilon" help="Approximation value for the FANTOM algorithm" type="number" value={this.state.epsilon} name="epsilon" onChange={this.handleChange}/>
              </Col>
              <Col md={4}>
              <SelectGroup id="formAlgorithm" label="Algorithm" options={["naive","hybrid","fantom-greedy","linear"]} value={this.state.algorithm} name="algorithm" onChange={this.handleChange}/>
              </Col>
            </Row>
            <Button onClick={this.handleSubmit}>
              Submit
            </Button>
          </Grid>
        </form>
      </Panel>
    );
  }
}

function InfoLine(props) {
  return (
    <Row className="show-grid">
      <Col md={4} className="inlineheader"><b>{props.name}</b></Col>
      <Col md={8}>{props.value}</Col>
    </Row>
  );
}

function Tuples(props) {
  if (props.value === null)
    return (<Label>Empty</Label>);
  else
    return (
      <div>
      {
        Object.entries(props.value).map((tuple) => {
          return (<Label className="tuple" key={JSON.stringify(tuple)}>{tuple[0]}: {tuple[1]}</Label>);
        })
      }
      </div>
    );
}

function Scopes(props) {
  return (<Grid>
    {
      (props.value || []).map((ContextTuples) => {
        return (
          <Row className="show-grid" key={JSON.stringify(ContextTuples)}>
            <Col md={4} className="inlineheader"><Tuples value={ContextTuples.context}/></Col>
            <Col md={8}>{
              ContextTuples.tuples.map((row) => {
                return (<Tuples value={row} key={JSON.stringify(row)}/>);
              })
            }</Col>
          </Row>
        );
      })
    }
    </Grid>);
}

function TestResult(props){
  var value = props.value || {};
  var result = value.result || {};
  var plan = result.plan || {};
  return (
    <Panel header="Test Result" key={value.id}>
      <Grid>
        <InfoLine name="Id:" value={value.id}/>
        <InfoLine name="Execution time:" value={result.executionTime}/>
        <InfoLine name="Speech cost:" value={plan.speechCost}/>
        <InfoLine name="Long form:" value={plan.longForm}/>
      </Grid>
      <br/>
      <Scopes value={plan.scopes}/>
    </Panel>
  );
}

export default App;
