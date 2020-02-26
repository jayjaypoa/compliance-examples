/*
 * @author Constantin Chelban (constantink@saltedge.com)
 * Copyright (c) 2020 Salt Edge.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.saltedge.connector.example.controllers;

import com.saltedge.connector.example.connector.ConnectorService;
import com.saltedge.connector.sdk.config.Constants;
import com.saltedge.connector.sdk.provider.ProviderCallback;
import com.saltedge.connector.sdk.provider.models.AccountData;
import com.saltedge.connector.sdk.provider.models.ProviderOfferedConsents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping(UserConsentController.BASE_PATH)
public class UserConsentController {
    public final static String BASE_PATH = "/oauth/authorize/consent";
    private static Logger log = LoggerFactory.getLogger(UserConsentController.class);
    @Autowired
    ConnectorService connectorService;
    @Autowired
    ProviderCallback providerCallback;

    @GetMapping
    public ModelAndView showConsentPage(
            @RequestParam(value = Constants.KEY_SESSION_SECRET) String sessionSecret,
            @RequestParam(name = Constants.KEY_USER_ID) String userId
    ) {
        List<AccountData> accounts = connectorService.getAccountsList(userId);
        ModelAndView result = new ModelAndView("sign_consent");
        result.addObject(Constants.KEY_SESSION_SECRET, sessionSecret);
        result.addObject(Constants.KEY_USER_ID, userId);
        result.addObject("accounts", accounts);
        return result;
    }

    @PostMapping
    public ModelAndView onConsentDataSubmit(
            @RequestParam(value = Constants.KEY_SESSION_SECRET) String sessionSecret,
            @RequestParam(name = Constants.KEY_USER_ID) String userId,
            @RequestParam List<String> balances,
            @RequestParam List<String> transactions
    ) {
        List<AccountData> accounts = connectorService.getAccountsList(userId);
        List<AccountData> balancesConsents = accounts.stream().filter(item -> balances.contains(item.id)).collect(Collectors.toList());
        List<AccountData> transactionsConsents = accounts.stream().filter(item -> transactions.contains(item.id)).collect(Collectors.toList());

        String returnToUrl = providerCallback.onOAuthAuthorizationSuccess(
                sessionSecret,
                userId,
                ProviderOfferedConsents.buildProviderOfferedConsents(balancesConsents, transactionsConsents)
        );

        return new ModelAndView((returnToUrl == null) ? "sign_error" : "redirect:" + returnToUrl);
    }
}