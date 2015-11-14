using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using InTheHand.Net.Bluetooth;
using InTheHand.Windows.Forms;
using InTheHand.Net.Sockets;
using InTheHand.Net.Bluetooth.AttributeIds;
using InTheHand.Net;
using System.IO;
using System.Runtime.InteropServices;
using System.Diagnostics;
using AutoItX3Lib;
using System.Threading;

namespace ControllerS
{
       
    public partial class Form1 : Form
    {
        Dictionary<string, string> dic = new Dictionary<string, string>();
      
       static readonly Guid MyServiceUuid = new Guid("{00001101-0000-1000-8000-00805F9B34FB}");
        static AutoItX3Lib.AutoItX3 au3 = new AutoItX3Lib.AutoItX3();
        [DllImport("user32.dll")]
        private static extern bool SetForegroundWindow(IntPtr hWnd);
         [DllImport("user32.dll", EntryPoint = "keybd_event", CharSet = CharSet.Auto, ExactSpelling = true)]
        public static extern void keybd_event(byte bVk, byte bScan, uint dwFlags, uint dwExtraInfo);
         bool connected, supported;
         Guid serviceClass;
         BluetoothClient conn;
        public Form1()
        {
            InitializeComponent();
            connected = false;
            label2.Text = "";
            label3.Text = "";
            label4.Text = "";
            button2.Visible = false;
            backgroundWorker1.WorkerReportsProgress = true;
            backgroundWorker1.WorkerSupportsCancellation = true;
            if (BluetoothRadio.IsSupported)
            {
                label1.Text = "Bluetooth Detected";
                supported = true;
            }
            else
            {
                label1.Text = "Switch on/Plugin bluetooth and then restart app";
                supported = false;
            }
            dic.Add("L","{LEFT}");
            dic.Add("R", "{RIGHT}");
            dic.Add("S", "{F5}");
            dic.Add("E", "{ESC}");
            dic.Add("W", "^{ESC}");
            dic.Add("s", "{LEFT}{LEFT}s");//sleep
            dic.Add("h", "{LEFT}{LEFT}h");//hibernate
            dic.Add("l", "#l");//lock
            dic.Add("d", "#d");//desktop
            dic.Add("r", "{LEFT}{LEFT}r");//restart
            dic.Add("c","{LEFT}{ENTER}");//shutdown;
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            
        }
        private void start_recieving()
        {
            Stream peerStream = conn.GetStream();
            byte[] buf = new byte[1];
            while (true)
            {
                int readLen = peerStream.Read(buf, 0, buf.Length);
                string val = ASCIIEncoding.ASCII.GetString(buf);
                if (readLen == 0)
                {
                    MessageBox.Show("Connection is closed");
                    label3.Text = "Connection Closed";
                    connected = false;
                    button1.Enabled = true;
                    button1.Text = "Start Server";
                    break;
                }
                else
                {
                    if (dic.ContainsKey(val))
                    {
                        au3.Send(dic[val]);            

                    }
                    else
                    {
                        label4.Text = "Please Report: Undefined " + val;
                    }
                               
                               
                }
            }
       
            
        }
        private void button1_Click(object sender, EventArgs e)
        {
            if (supported && !connected)
            {
                backgroundWorker1.RunWorkerAsync();   
                //MessageBox.Show("Pair your Android with Windows machine using Bluetooth, If not already Done.");
                
                
                //start_recieving();
            }
            
        }

        private void button2_Click(object sender, EventArgs e)
        {
            string val = "#d";
            au3.Send(val);
               
        }

        private void backgroundWorker1_DoWork(object sender, DoWorkEventArgs e)
        {
            BackgroundWorker worker = sender as BackgroundWorker;
            serviceClass = BluetoothService.SerialPort;
            var lsnr = new BluetoothListener(serviceClass);
            lsnr.Start();
            worker.ReportProgress(0);
            conn = lsnr.AcceptBluetoothClient();
            worker.ReportProgress(1);
            connected = true;
            Stream peerStream = conn.GetStream();
            byte[] buf = new byte[1];
            while (true)
            {
                int readLen = peerStream.Read(buf, 0, buf.Length);
                string val = ASCIIEncoding.ASCII.GetString(buf);
                if (readLen == 0)
                {
                    worker.ReportProgress(2);
                    break;
                }
                else
                {
                    if (dic.ContainsKey(val))
                    {
                        au3.Send(dic[val]);

                    }
                  /*  else
                    {
                        worker.ReportProgress(3);
                        label4.Text = "Please Report: Undefined " + val;
                    }
                 */

                }
            }
        }

        private void backgroundWorker1_ProgressChanged(object sender, ProgressChangedEventArgs e)
        {
            if (e.ProgressPercentage == 0)
            {
                label2.Text = "Server started";
                label3.Text="Waiting for connection...";
                label4.Text = "Connect From Android Mobile";
                button1.Text = "Server Started";
                button1.Enabled = false;
            }
            else if(e.ProgressPercentage == 1)
            {
                label3.Text = "Connection established";
                label4.Text = "Open powerpoint and bring it in foreground";
            }
            else if (e.ProgressPercentage == 2)
            {
                MessageBox.Show("Connection is closed");
                label3.Text = "Connection Closed";
                label2.Text = "";
                connected = false;
                button1.Enabled = true;
                button1.Text = "Start Server";
            }
        }

        private void backgroundWorker1_RunWorkerCompleted(object sender, RunWorkerCompletedEventArgs e)
        {
            if (e.Error != null)
            {
                label3.Text = "Something bad Happened, Restart APP";
               
            }
        }
    }
}
