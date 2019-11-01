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
import java.io.*;
import java.util.*;
import javax.swing.*;

/**
 * WaitGUI appears to the users after they have been logged in to EODMS.
 * The user is prompted by the GUI to enter their Search Parameters into the EODMS interface, and then press the START button on the GUI.
 * 
 * @author Kieran Moynihan
 */
public class WaitGUI extends AbstractGUI {
	//button and txtfield
	JFrame frame = new JFrame("Waiting...");
	private done = false;
	private font = new Font("Arial", Font.BOLD, 14);
	private JPanel panel = new JPanel(new BorderLayout());
	private JLabel label = new JLabel("Enter Search Parameters into EODMS interface, then press START", SwingConstants.CENTER);
	private JButton button = new JButton("START");
	private int x,y;

	private void GUI() {
		frame.setSize(400, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((x - frame.getHeight()).intdiv(2),(y - frame.getHeight()).intdiv(2));
		panel.add(label, BorderLayout.CENTER);
		panel.add(button, BorderLayout.SOUTH);

		//JFRAME
		frame.add(panel);
		frame.setVisible(true);

		button.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent u) {
						setDone();
					}
				}
				);

	}

	public WaitGUI(){
		Screen();
		GUI();
	}
}
