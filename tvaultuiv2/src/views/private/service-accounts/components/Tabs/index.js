/* eslint-disable no-console */
/* eslint-disable react/jsx-props-no-spreading */
import React, { useState, useEffect, useCallback } from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';

import { makeStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import ComponentError from '../../../../../errorBoundaries/ComponentError/component-error';
import mediaBreakpoints from '../../../../../breakpoints';
import ServiceAccountSecrets from '../ServiceAccountSecrets';
import ServiceAccountPermission from '../ServiceAccountPermission';
import { useStateValue } from '../../../../../contexts/globalState';
import apiService from '../../apiService';
// styled components goes here

const TabPanelWrap = styled.div`
  position: relative;
  height: 100%;
  margin: 0;
  padding-top: 1.3rem;
  ${mediaBreakpoints.small} {
    height: 77vh;
  }
`;

const TabContentsWrap = styled('div')`
  height: calc(100% - 4.8rem);
`;

const TabPanel = (props) => {
  const { children, value, index } = props;

  return (
    <TabPanelWrap
      role="tabpanel"
      hidden={value !== index}
      id={`safes-tabpanel-${index}`}
      aria-labelledby={`safe-tab-${index}`}
    >
      {children}
    </TabPanelWrap>
  );
};

TabPanel.propTypes = {
  children: PropTypes.node,
  index: PropTypes.number.isRequired,
  value: PropTypes.number.isRequired,
};

TabPanel.defaultProps = {
  children: <div />,
};

function a11yProps(index) {
  return {
    id: `safety-tab-${index}`,
    'aria-controls': `safety-tabpanel-${index}`,
  };
}

const useStyles = makeStyles((theme) => ({
  root: {
    flexGrow: 1,
    padding: '0 2.1rem',
    height: 'calc( 100% - 19.1rem )',
    display: 'flex',
    flexDirection: 'column',
    background: 'linear-gradient(to bottom,#151820,#2c3040)',
  },
  appBar: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between',
    height: '4.8rem',
    boxShadow: 'none',
    borderBottom: '0.3rem solid #222632',
    [theme.breakpoints.down('md')]: {
      height: 'auto',
    },
  },
  tab: {
    minWidth: '9.5rem',
  },
}));

const AccountSelectionTabs = (props) => {
  const { accountDetail } = props;
  const classes = useStyles();
  const [value, setValue] = useState(0);
  const [response, setResponse] = useState({ status: 'loading' });
  const [hasPermission, setHasPermission] = useState(false);
  const [accountMetaData, setAccountMetaData] = useState({
    response: {},
    error: '',
  });
  const [state] = useStateValue();
  const handleChange = (event, newValue) => {
    setValue(newValue);
  };

  const fetchPermission = useCallback(() => {
    setResponse({ status: 'loading' });
    apiService
      .updateMetaPath(accountDetail.name)
      .then((res) => {
        console.log('res', res);
        if (res.data && res.data.data) {
          setResponse({ status: 'success' });
          if (res.data.data.managedBy === state.username) {
            setHasPermission(true);
            setAccountMetaData({ response: { ...res.data.data }, error: '' });
          } else {
            setHasPermission(false);
          }
        }
      })
      .catch(() => {
        setResponse({ status: 'error' });
        setAccountMetaData({ response: {}, error: 'Something went wrong' });
      });
  }, [accountDetail, state]);

  useEffect(() => {
    setResponse({ status: 'loading' });
    setHasPermission(false);
    if (accountDetail?.name) {
      fetchPermission();
    }
  }, [accountDetail, fetchPermission]);

  return (
    <ComponentError>
      <div className={classes.root}>
        <AppBar position="static" className={classes.appBar}>
          <Tabs
            value={value}
            onChange={handleChange}
            aria-label="safe tabs"
            indicatorColor="secondary"
            textColor="primary"
          >
            <Tab className={classes.tab} label="Secrets" {...a11yProps(0)} />
            {accountDetail.admin && (
              <Tab
                label="Permissions"
                {...a11yProps(1)}
                disabled={!accountDetail.admin}
              />
            )}
          </Tabs>
        </AppBar>
        <TabContentsWrap>
          <TabPanel value={value} index={0}>
            <ServiceAccountSecrets
              accountDetail={accountDetail}
              accountMetaData={accountMetaData}
            />
          </TabPanel>
          <TabPanel value={value} index={1}>
            <ServiceAccountPermission
              accountDetail={accountDetail}
              accountMetaData={accountMetaData}
              hasPermission={hasPermission}
              parentStatus={response.status}
              refresh={fetchPermission}
            />
          </TabPanel>
        </TabContentsWrap>
      </div>
    </ComponentError>
  );
};
AccountSelectionTabs.propTypes = {
  accountDetail: PropTypes.objectOf(PropTypes.any),
};
AccountSelectionTabs.defaultProps = {
  accountDetail: {},
};

export default AccountSelectionTabs;
