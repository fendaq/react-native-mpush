import React, { Component } from "react";
import {
    AppRegistry, Button, DeviceEventEmitter, StatusBar, StyleSheet, Text, TextInput, ToastAndroid, View
} from "react-native";

import MPush from "react-native-mpush";

const publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCghPCWCobG8nTD24juwSVataW7iViRxcTkey/B792VZEhuHjQvA3cAJgx2Lv8GnX8NIoShZtoCg3Cx6ecs+VEPD2fBcg2L4JK7xldGpOJ3ONEAyVsLOttXZtNXvyDZRijiErQALMTorcgi79M5uVX9/jMv2Ggb2XAeZhlLD28fHwIDAQAB";
const version = "1.0.0";
const tags = "group";

MPush.startPush({
    allocServer: "http://103.60.220.145:9999",
    userId: "RN_USER",
    tags: tags,
    publicKey: publicKey,
    version: version,
});

/**
 *
 * @author tangzehua
 * @since 2017-03-03 12:40
 */
export default class App extends Component {

    constructor(props) {
        super(props);
        this.userId = "RN_USER";
        this.sendUserId = this.userId;
        this.content = "Hello React Native MPush";
        this.allocServer = "http://103.60.220.145:9999";
    }

    componentDidMount() {
        // this.startPush();
        DeviceEventEmitter.addListener("MPushEventMessage", message => {
            ToastAndroid.show(message, ToastAndroid.SHORT);
            console.log(message);
        })
    }

    sendPush() {
        MPush.sendPush({
            userId: this.sendUserId,
            hello: this.content
        })
            .then(message => {
                console.log(message);
                ToastAndroid.show(message, ToastAndroid.SHORT);
            })
            .catch(e => {
                console.log(e);
                ToastAndroid.show(e, ToastAndroid.SHORT);
            });
    }

    bindUser() {
        MPush.bindUser(this.userId, "group");
    }

    unBindUser() {
        MPush.unbindUser();
    }

    startPush() {
        MPush.startPush({
            allocServer: this.allocServer,
            userId: this.userId,
            tags: tags,
            publicKey: publicKey,
            version: version,
        });
    }

    stopPush() {
        MPush.stopPush();
    }

    pausePush() {
        MPush.pausePush();
    }

    resumePush() {
        MPush.resumePush();
    }

    render() {
        return (
            <View style={styles.container}>
                <StatusBar backgroundColor='#303f9f' barStyle={"light-content"}/>
                <View style={styles.topBar}>
                    <Text style={styles.welcome}>React Native MPush</Text>
                </View>
                <Text style={styles.text}>
                  Hello MPush
                </Text>
                <View style={styles.input1}>
                    <Text style={styles.text2}>Allocator:</Text>
                    <TextInput style={styles.input2} value={this.allocServer}
                               onChangeText={allocServer => this.allocServer = allocServer}/>
                </View>
                <View style={styles.input1}>
                    <Text style={styles.text2}>from:</Text>
                    <TextInput style={styles.input2} value={this.userId} onChangeText={text => this.userId = text}/>
                    <Button onPress={() => this.bindUser()} title="BIND"/>
                    <View style={{width: 10}}/>
                    <Button onPress={() => this.unBindUser()} title="UNBIND"/>
                </View>
                <View style={styles.input1}>
                    <Text style={styles.text2}>to:</Text>
                    <TextInput style={styles.input2} value={this.sendUserId}
                               onchangetext={userId => this.sendUserId = userId}/>
                </View>
                <View style={styles.input1}>
                    <Text style={styles.text2}>say:</Text>
                    <TextInput style={styles.input2} value={this.content}
                               onChangeText={content => this.content = content}/>
                    <Button onPress={() => this.sendPush()} title="SEND"/>
                </View>
                <View style={[styles.input1, {justifyContent: 'center', paddingTop: 15}] }>
                    <Button onPress={() => this.startPush()} title="START"/>
                    <View style={{width: 10}}/>
                    <Button onPress={() => this.stopPush()} title="STOP"/>
                    <View style={{width: 10}}/>
                    <Button onPress={() => this.pausePush} title="PAUSE"/>
                    <View style={{width: 10}}/>
                    <Button onPress={() => this.resumePush()} title="RESUME"/>
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#F5FCFF',
    },

    topBar: {
        backgroundColor: '#3f51b5', height: 60, width: '100%',
        alignItems: 'flex-start', justifyContent: 'center',
        marginBottom: 16,
    },

    welcome: {
        color: '#fff',
        fontSize: 20,
        textAlign: 'left',
        margin: 10,
    },
    text: {
        textAlign: 'left',
        marginLeft: 15,
        fontSize: 18,
    },

    text2: {
        fontSize: 16,
    },

    button1: {},

    input1: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 15,
        paddingVertical: 0,
    },
    input2: {
        flex: 1,
    }
});

AppRegistry.registerComponent('example', () => App);