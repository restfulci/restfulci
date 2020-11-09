describe('Hello-world Git Job Test', () => {
  it('Create a job and a run and delete a job', () => {
    cy.visit('http://localhost:3000/jobs')

    cy.url().should('match', /.*\/login$/)
    cy.get('#username').type('test-user')
    cy.get('#password').type('password')
    cy.contains('Log in').click()

    cy.url().should('match', /.*\/jobs$/)

    cy.contains('+').click()
    cy.url().should('match', /.*\/jobs\/add.*$/)
    cy.get('#name').type('cypress_git_job')
    cy.get('#remoteOrigin').type('https://github.com/restfulci/restfulci-examples.git')
    cy.get('#configFilepath').type('hello-world/restfulci.yml')
    cy.contains('Add git job').click()

    cy.url().should('match', /.*\/jobs\/\d+$/)
    cy.contains('cypress_git_job')

    cy.visit('http://localhost:3000/jobs')
    cy.contains('cypress_git_job').click()

    cy.url().should('match', /.*\/jobs\/\d+$/)
    cy.contains('Trigger a run').click()
    cy.get('#branchName').type('master')
    cy.contains('Trigger it').click()
    cy.url().should('match', /.*\/jobs\/\d+\/runs\/\d+$/)
    cy.contains('⌛')

    function reloadReq () {
      cy.reload().wait(1000)
        .then((resp) => {
          cy.get('body').then(($body) => {
              if ($body.text().includes('⌛')) {
                reloadReq()
              }
            })
        })
    }

    cy.then(reloadReq)
    cy.contains('✅')
    cy.contains('Completed at')
    cy.contains('Exit code')

    cy.contains('cypress_git_job').click()
    cy.url().should('match', /.*\/jobs\/\d+$/)
    cy.contains("Settings").click()

    cy.url().should('match', /.*\/jobs\/\d+\/settings$/)
    cy.contains("Delete job").click()

    cy.url().should('match', /.*\/jobs$/)
    cy.contains('cypress_git_job').should('not.exist')
  })
})
