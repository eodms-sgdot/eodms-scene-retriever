import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.checkpoint.CheckpointFactory
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testcase.TestCaseFactory
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testdata.TestDataFactory
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords

import internal.GlobalVariable

import MobileBuiltInKeywords as Mobile
import WSBuiltInKeywords as WS
import WebUiBuiltInKeywords as WebUI

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * First GUI presented to user.
 * User is to enter EODMS login credentials into this GUI for future login attempts by program.
 * 
 * @author Kieran Moynihan, Khang Nguyen
 */
public class StartGUI extends AbstractGUI {
	//button and txtfield
	JFrame frame = new JFrame("Login Info");
	private String username,password;
	private done = false;
	private font = new Font("Arial", Font.BOLD, 14)
	private JPanel[] panel = new JPanel[8];
	private JTextField textUser = new JTextField('Username');
	private JPasswordField textPass = new JPasswordField('Password');
	private JLabel[] label = new JLabel[2];
	private JButton button = new JButton("LOGIN");
	private int x,y;

	private void GUI() {
		frame.setSize(400, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((x - frame.getHeight()).intdiv(2),(y - frame.getHeight()).intdiv(2));

		//WEST TOP
		panel[1] = new JPanel(new FlowLayout());
		label[0] = new JLabel("UserName");
		label[0].setFont(font);
		panel[1].add(label[0]);
		//WEST BOT
		panel[2] = new JPanel(new FlowLayout());
		label[1] = new JLabel("Password");
		label[1].setFont(font);
		panel[2].add(label[1]);
		panel[3] = new JPanel(new GridLayout(2,1));
		panel[3].add(panel[1]);
		panel[3].add(panel[2]);

		//CENTER TOP
		panel[4] = new JPanel(new FlowLayout());
		textUser.setColumns(12);
		panel[4].add(textUser);
		//CENTER BOT
		panel[5] = new JPanel(new FlowLayout());
		textPass.setColumns(12);
		textPass.setEchoChar('*'.toCharArray()[0]);
		panel[5].add(textPass);
		panel[6] = new JPanel(new GridLayout(2,1));
		panel[6].add(panel[4]);
		panel[6].add(panel[5]);

		//SOUTH
		panel[7] = new JPanel(new FlowLayout());
		panel[7].add(button);

		//JFRAME
		panel[0] = new JPanel(new BorderLayout());
		panel[0].add(panel[6], BorderLayout.CENTER);
		panel[0].add(panel[3], BorderLayout.WEST);
		panel[0].add(panel[7], BorderLayout.SOUTH);
		frame.add(panel[0]);
		frame.setVisible(true);

		button.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent u) {
						username = textUser.getText();
						password = new String(textPass.getPassword());
						setDone();
					}
				}
				);

		textUser.addFocusListener(
				new FocusListener() {
					public void focusGained(FocusEvent e) {
						if (textUser.getText() == 'Username') {
							textUser.setText('');
						}
					}
					public void focusLost(FocusEvent e) {
						pass;
					}
				}
				);

		textPass.addFocusListener(
				new FocusListener() {
					public void focusGained(FocusEvent e) {
						if (new String(textPass.getPassword()) == 'Password') {
							textPass.setText('');
						}
					}
					public void focusLost(FocusEvent e) {
						pass;
					}
				}
				);

	}
	//return username
	public String getUsername(){
		return username;
	}
	//return password
	public String getPassword(){
		return password;
	}
	//set username
	public String setUsername(username){
		this.username = username;
	}
	//set password
	public String setPassword(password){
		this.password = password;
	}

	public StartGUI(){
		Screen();
		GUI();
	}
}
