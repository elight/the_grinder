// Copyright (C) 2000 Paco Gomez
// Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.console;

import java.io.File;

import net.grinder.common.GrinderException;
import net.grinder.communication.Message;
import net.grinder.console.common.ConsoleException;
import net.grinder.console.common.Resources;
import net.grinder.console.communication.ConsoleCommunication;
import net.grinder.console.messages.RegisterStatisticsViewMessage;
import net.grinder.console.messages.RegisterTestsMessage;
import net.grinder.console.messages.ReportStatisticsMessage;
import net.grinder.console.model.ConsoleProperties;
import net.grinder.console.model.Model;
import net.grinder.console.swingui.ConsoleUI;
import net.grinder.statistics.StatisticsView;


/**
 * This is the entry point of The Grinder console.
 *
 * @author Paco Gomez
 * @author Philip Aston
 * @version $Revision$
 **/
public class Console {

  private final ConsoleCommunication m_communication;
  private final Model m_model;
  private final ConsoleUI m_userInterface;

  /**
   * Constructor.
   *
   * @exception GrinderException If an error occurs.
   */
  public Console() throws GrinderException {

    String homeDirectory = System.getProperty("user.home");

    if (homeDirectory == null) {
      // Some platforms do not have user home directories.
      homeDirectory = System.getProperty("java.home");
    }

    final Resources resources =
      new Resources("net.grinder.console.swingui.resources.Console");

    final ConsoleProperties properties =
      new ConsoleProperties(resources,
                            new File(homeDirectory, ".grinder_console"));

    m_model = new Model(properties, resources);

    m_communication = new ConsoleCommunication(resources, properties);

    m_userInterface =
      new ConsoleUI(m_model, m_communication.getProcessControl());

    m_communication.setErrorHandler(m_userInterface.getErrorHandler());

    m_communication.addMessageHandler(
      new ConsoleCommunication.MessageHandler() {
        public boolean process(Message message) throws ConsoleException {
          if (message instanceof RegisterTestsMessage) {
            m_model.registerTests(((RegisterTestsMessage)message).getTests());
            return true;
          }

          if (message instanceof ReportStatisticsMessage) {
            m_model.addTestReport(
              ((ReportStatisticsMessage)message).getStatisticsDelta());
            return true;
          }

          if (message instanceof RegisterStatisticsViewMessage) {
            final StatisticsView statisticsView =
              ((RegisterStatisticsViewMessage)message).getStatisticsView();

            m_model.registerStatisticsViews(statisticsView, statisticsView);
            return true;
          }

          return false;
        }
      });
  }

  /**
   * Console message event loop. Dispatches communication messages
   * appropriately.
   *
   * @exception ConsoleException If an error occurs.
   */
  public void run() throws ConsoleException {
    while (true) {
      m_communication.processOneMessage();
    }
  }
}
